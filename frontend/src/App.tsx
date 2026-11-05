import { Navigate, Route, Routes } from "react-router";
import { AppLayout } from "./components/AppLayout";
import { RequireSession, RequireNoSession } from "./components/SessionGates";
import { Sky } from "./components/Sky";
import { FeedPage } from "./pages/FeedPage";
import { LoginPage } from "./pages/LoginPage";
import { MyPostsPage } from "./pages/MyPostsPage";
import { RegisterPage } from "./pages/RegisterPage";
import { WritePage } from "./pages/WritePage";

// Every page in the app is listed here.
export function App() {
  return (
    <>
      <Sky />
      <Routes>
        <Route element={<RequireNoSession />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>

        <Route element={<RequireSession />}>
          <Route element={<AppLayout />}>
            <Route index element={<FeedPage />} />
            <Route path="/write" element={<WritePage />} />
            <Route path="/mine" element={<MyPostsPage />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}
