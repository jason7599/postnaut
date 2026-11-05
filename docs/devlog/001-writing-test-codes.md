# Writing Test Codes

I think I've been dramatically over-dreading writing test code. Turns out it isn't too hard, and it's kinda fun, too.

## The Setup

Testcontainers, the GOAT. Basically sets up dummy containers for postgresql and redis. 

```java
@SpringBootTest         // starts the full spring application
@AutoConfigureMockMvc   // mockmvc to send fake HTTP requests without opening a real port
@Import({TestClient.class, UserFixtures.class})
public abstract class IntegrationTest {

    @ServiceConnection  // Points my app's database config at this container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @SuppressWarnings("resource")
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }
```

And MockMvc. 

```java
@TestComponent
@RequiredArgsConstructor
public class TestClient {

    private final MockMvc mvc;
    private final ObjectMapper objectMapper;

    public ResultActions post(String path, Object body, Cookie cookie) throws Exception {
        var request = MockMvcRequestBuilders.post(path);

        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body));
        }
        if (cookie != null) request.cookie(cookie);

        return mvc.perform(request);
    }

    public ResultActions get(String path, Cookie cookie) throws Exception {
        var request = MockMvcRequestBuilders.get(path);

        if (cookie != null) request.cookie(cookie);

        return mvc.perform(request);
    }
}
```

Instead of testing the service methods, we test against the controllers. Which I think is definitely more apt - a proper blackbox test. Which is exactly how the caller (FE) will call it anyway.


## Actually getting to reproduce a race condition

Never thought it possible. Or at least, not this easily. But using thread pools and repeated tests, it was surprisingly easy.

Baby's first stress test

```java
@RepeatedTest(50)
void createPost_stress_neverExceedsLiveCap() throws Exception {
    TestUser user = users.create("jason");
    final int requests = PostConstraints.MAX_LIVE_POSTS * 3;

    // CountDownLatch: A gate that opens when a counter reaches zero
    CountDownLatch start = new CountDownLatch(1);
    List<Future<Integer>> futures = new ArrayList<>();

    try (ExecutorService executor = Executors.newFixedThreadPool(requests)) {
        // Queue tasks
        for (int i = 0; i < requests; i++) {
            futures.add(executor.submit(() -> {
                // every thread waits here until released together by the latch
                start.await();

                return createPost(user.session())
                        .andReturn().getResponse().getStatus();
            }));
        }

        start.countDown();

        for (Future<Integer> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
    }

    assertPostCount(PostConstraints.MAX_LIVE_POSTS);
}
```

It was pretty marvelous to see how it actually fails sometimes if I remove the lock in the service method. Amazinnnnnnnnnng. Great interview yapping material