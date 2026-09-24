import { ROLE_PAGES } from "@/lib/roles";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useEffect } from "react";
import { useAuth } from "@/context/auth-context";

export const Route = createFileRoute("/")({
  component: IndexPage,
});

function IndexPage() {
  const { user, isAuthenticated, isLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoading) {
      if (isAuthenticated && user?.role) {
        navigate({ to: ROLE_PAGES[user.role].path, replace: true });
      } else {
        navigate({ to: "/login", replace: true });
      }
    }
  }, [isLoading, isAuthenticated, user, navigate]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-background text-foreground">
      <div className="flex flex-col items-center gap-3">
        <div className="size-8 animate-spin rounded-full border-2 border-primary border-t-transparent" />
        <span className="text-sm font-medium text-muted-foreground">
          Đang chuyển hướng hệ thống...
        </span>
      </div>
    </div>
  );
}
