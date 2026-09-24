import { useState, useMemo, useEffect, type ReactNode } from "react";
import { Link, useRouterState, useNavigate } from "@tanstack/react-router";
import {
  Trophy,
  Menu,
  X,
  Search,
  Bell,
  CheckCheck,
  Trash2,
  ChevronDown,
  LogOut,
  ShieldCheck,
} from "lucide-react";
import { useAuth, DEMO_USERS } from "@/context/auth-context";
import { useStable } from "@/context/stable-context";
import { ROLE_PAGES, type Role } from "@/lib/roles";

export function AppLayout({ children, role }: { children: ReactNode; role: Role }) {
  const { user, isAuthenticated, isLoading, logout } = useAuth();
  const { horses, notifications, markAllNotificationsRead, clearNotifications } = useStable();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [profileDropdownOpen, setProfileDropdownOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchModalOpen, setSearchModalOpen] = useState(false);

  const routerState = useRouterState();
  const activeSection = routerState.location.hash || "overview";
  const page = ROLE_PAGES[role];
  const RoleIcon = page.icon;

  // Keep every workspace tied to the signed-in role, including direct URLs.
  useEffect(() => {
    if (isLoading) return;
    if (!isAuthenticated) {
      navigate({ to: "/login", replace: true });
    } else if (user && user.role !== role) {
      navigate({ to: ROLE_PAGES[user.role].path, replace: true });
    }
  }, [isLoading, isAuthenticated, user, role, navigate]);

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  const filteredHorses = useMemo(() => {
    if (!searchQuery.trim()) return [];
    const q = searchQuery.toLowerCase();
    return horses.filter(
      (h) =>
        h.name.toLowerCase().includes(q) ||
        h.stall.toLowerCase().includes(q) ||
        h.status.toLowerCase().includes(q),
    );
  }, [horses, searchQuery]);

  const filteredStaff = useMemo(() => {
    if (role !== "manager" || !searchQuery.trim()) return [];
    const q = searchQuery.toLowerCase();
    return Object.values(DEMO_USERS).filter(
      (u) =>
        u.name.toLowerCase().includes(q) ||
        u.roleTitle.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q),
    );
  }, [searchQuery, role]);

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background text-foreground">
        <div className="flex flex-col items-center gap-3">
          <div className="size-8 animate-spin rounded-full border-2 border-primary border-t-transparent" />
          <span className="text-sm font-medium text-muted-foreground">Đang tải phân hệ...</span>
        </div>
      </div>
    );
  }

  if (!isAuthenticated || !user || user.role !== role) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background text-foreground">
      {/* Mobile backdrop */}
      {sidebarOpen && (
        <button
          aria-label="Đóng menu"
          className="fixed inset-0 z-30 bg-overlay/80 lg:hidden animate-fade-in"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Left Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-[276px] flex-col bg-sidebar text-sidebar-foreground transition-transform duration-300 lg:translate-x-0 ${
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="flex h-20 items-center gap-3 border-b border-sidebar-border px-6">
          <div className="grid size-11 place-items-center rounded-md bg-sidebar-primary text-sidebar-primary-foreground shadow-sm">
            <Trophy size={22} />
          </div>
          <div>
            <div className="font-display text-xl font-bold tracking-tight">MÃ PHONG</div>
            <div className="text-[11px] uppercase tracking-[0.18em] text-sidebar-muted">
              Quản lý ngựa đua
            </div>
          </div>
          <button
            aria-label="Đóng menu"
            className="ml-auto text-sidebar-muted hover:text-white lg:hidden"
            onClick={() => setSidebarOpen(false)}
          >
            <X size={20} />
          </button>
        </div>

        <div className="mx-4 mt-5 flex items-center gap-3 rounded-lg border border-sidebar-border bg-sidebar-panel p-3">
          <RoleIcon size={22} className="shrink-0 text-sidebar-primary" />
          <div>
            <p className="text-sm font-bold">{page.label}</p>
            <p className="mt-1 text-[11px] text-sidebar-muted">{page.description}</p>
          </div>
        </div>
        <nav
          className="flex-1 space-y-1.5 overflow-y-auto p-4"
          aria-label={`Điều hướng ${page.label}`}
        >
          <p className="px-3 pb-2 pt-2 text-[10px] font-semibold uppercase tracking-[0.16em] text-sidebar-muted">
            Không gian làm việc
          </p>
          {page.sections.map((item) => {
            const Icon = item.icon;
            const active = activeSection === item.id;
            return (
              <Link
                key={item.id}
                to={page.path}
                hash={item.id}
                activeOptions={{ includeHash: true }}
                activeProps={{ "aria-current": "location" }}
                aria-current={active ? "location" : undefined}
                onClick={() => {
                  setSidebarOpen(false);
                }}
                className={`group grid w-full grid-cols-[40px_1fr] items-center gap-2 rounded-md px-3 py-3 text-left transition ${
                  active
                    ? "bg-sidebar-accent text-sidebar-accent-foreground shadow-sidebar font-semibold"
                    : "text-sidebar-muted hover:bg-sidebar-hover hover:text-sidebar-foreground"
                }`}
              >
                <span
                  className={`grid size-9 place-items-center rounded-md transition ${
                    active
                      ? "bg-sidebar-primary text-sidebar-primary-foreground"
                      : "bg-sidebar-icon group-hover:text-white"
                  }`}
                >
                  <Icon size={18} />
                </span>
                <span className="min-w-0">
                  <span className="block truncate text-sm">{item.label}</span>
                </span>
              </Link>
            );
          })}
        </nav>

        {/* Quick System Status */}
        <div className="m-4 rounded-md border border-sidebar-border bg-sidebar-panel p-4">
          <div className="mb-2 flex items-center justify-between text-xs font-semibold">
            <span className="flex items-center gap-2">
              <ShieldCheck size={15} className="text-sidebar-primary" /> Hệ thống trực tuyến
            </span>
            <span className="inline-flex size-2 rounded-full bg-success animate-pulse" />
          </div>
          <p className="text-[11px] leading-relaxed text-sidebar-muted">
            {horses.filter((h) => h.status === "Khỏe").length}/{horses.length} chiến mã khỏe mạnh •
            5/5 phân hệ hoạt động
          </p>
        </div>
      </aside>

      {/* Main Container */}
      <div className="lg:pl-[276px]">
        {/* Top Header */}
        <header className="sticky top-0 z-20 grid h-20 grid-cols-[auto_minmax(0,1fr)_auto] items-center gap-3 border-b border-border bg-background/95 px-4 backdrop-blur-md sm:px-6 lg:grid-cols-[minmax(0,1fr)_auto] lg:px-8">
          <button
            aria-label="Mở menu"
            className="grid size-10 place-items-center rounded-md border border-border hover:bg-muted lg:hidden"
            onClick={() => setSidebarOpen(true)}
          >
            <Menu size={20} />
          </button>

          {/* Search bar */}
          <div className="relative min-w-0 sm:max-w-md">
            <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <input
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                if (e.target.value.trim().length > 0) setSearchModalOpen(true);
              }}
              onFocus={() => {
                if (searchQuery.trim().length > 0) setSearchModalOpen(true);
              }}
              className="h-10 w-full rounded-md border border-input bg-muted/60 pl-9 pr-3 text-sm outline-none transition focus:border-primary focus:bg-background focus:ring-2 focus:ring-ring"
              aria-label="Tìm kiếm trong phân hệ"
              placeholder={
                role === "manager" ? "Tìm chiến mã, nhân sự..." : "Tìm chiến mã, ô chuồng..."
              }
            />
            {searchQuery && (
              <button
                onClick={() => {
                  setSearchQuery("");
                  setSearchModalOpen(false);
                }}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-xs text-muted-foreground hover:text-foreground"
              >
                <X size={15} />
              </button>
            )}

            {/* Instant Search Dropdown */}
            {searchModalOpen && searchQuery.trim().length > 0 && (
              <div className="absolute left-0 right-0 top-12 z-50 max-h-96 overflow-y-auto rounded-lg border border-border bg-card p-3 shadow-modal animate-scale-in">
                <div className="mb-2 flex items-center justify-between border-b border-border pb-2 text-xs font-bold text-muted-foreground">
                  <span>Kết quả tìm kiếm: "{searchQuery}"</span>
                  <button
                    onClick={() => setSearchModalOpen(false)}
                    className="hover:text-foreground"
                  >
                    <X size={14} />
                  </button>
                </div>

                {filteredHorses.length === 0 && filteredStaff.length === 0 ? (
                  <p className="py-4 text-center text-xs text-muted-foreground">
                    Không tìm thấy kết quả phù hợp.
                  </p>
                ) : (
                  <div className="space-y-3">
                    {filteredHorses.length > 0 && (
                      <div>
                        <p className="mb-1 text-[10px] font-bold uppercase text-primary">
                          Chiến mã ({filteredHorses.length})
                        </p>
                        {filteredHorses.map((h) => (
                          <div
                            key={h.stall}
                            className="flex items-center justify-between rounded-md p-2 text-sm"
                          >
                            <span className="font-semibold">{h.name}</span>
                            <span className="text-xs text-muted-foreground">
                              Ô {h.stall} • {h.status}
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                    {filteredStaff.length > 0 && (
                      <div>
                        <p className="mb-1 text-[10px] font-bold uppercase text-primary">
                          Nhân sự ({filteredStaff.length})
                        </p>
                        {filteredStaff.map((st) => (
                          <Link
                            key={st.id}
                            to="/manager"
                            hash="operations"
                            onClick={() => {
                              setSearchModalOpen(false);
                            }}
                            className="flex cursor-pointer items-center justify-between rounded-md p-2 text-sm hover:bg-muted"
                          >
                            <span className="font-semibold">{st.name}</span>
                            <span className="text-xs text-muted-foreground">
                              {st.roleTitle} ({st.email})
                            </span>
                          </Link>
                        ))}
                      </div>
                    )}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Header Right Actions */}
          <div className="flex items-center gap-2 sm:gap-4">
            {/* Notifications Bell */}
            <div className="relative">
              <button
                aria-label="Thông báo"
                onClick={() => setNotificationsOpen(!notificationsOpen)}
                className="relative grid size-10 place-items-center rounded-md border border-border hover:bg-muted transition"
              >
                <Bell size={19} />
                {unreadCount > 0 && (
                  <span className="absolute -right-1 -top-1 flex size-4 items-center justify-center rounded-full bg-danger text-[10px] font-bold text-white ring-2 ring-background animate-pulse">
                    {unreadCount}
                  </span>
                )}
              </button>

              {/* Notification Menu */}
              {notificationsOpen && (
                <div className="absolute right-0 top-12 z-50 w-80 sm:w-96 rounded-lg border border-border bg-card p-4 shadow-modal animate-scale-in">
                  <div className="mb-3 flex items-center justify-between border-b border-border pb-2">
                    <div className="flex items-center gap-2 font-display text-sm font-bold">
                      <Bell size={16} className="text-primary" /> Thông báo ({unreadCount})
                    </div>
                    <div className="flex gap-1 text-xs">
                      <button
                        onClick={markAllNotificationsRead}
                        title="Đánh dấu tất cả đã đọc"
                        className="rounded p-1 hover:bg-muted text-muted-foreground hover:text-foreground"
                      >
                        <CheckCheck size={16} />
                      </button>
                      <button
                        onClick={clearNotifications}
                        title="Xóa tất cả"
                        className="rounded p-1 hover:bg-muted text-muted-foreground hover:text-danger"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </div>

                  <div className="max-h-72 space-y-2 overflow-y-auto">
                    {notifications.length === 0 ? (
                      <p className="py-6 text-center text-xs text-muted-foreground">
                        Không có thông báo mới.
                      </p>
                    ) : (
                      notifications.map((notif) => (
                        <div
                          key={notif.id}
                          className={`rounded-md border p-2.5 text-xs transition ${
                            notif.isRead
                              ? "border-border bg-muted/20 opacity-75"
                              : notif.type === "danger"
                                ? "border-danger/30 bg-danger-soft text-danger"
                                : "border-primary/30 bg-primary-soft text-foreground"
                          }`}
                        >
                          <div className="flex justify-between font-bold">
                            <span>{notif.title}</span>
                            <span className="text-[10px] opacity-70">{notif.time}</span>
                          </div>
                          <p className="mt-1 leading-relaxed">{notif.desc}</p>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}
            </div>

            <div className="hidden h-8 w-px bg-border sm:block" />

            {/* User Profile dropdown */}
            {user && (
              <div className="relative">
                <button
                  onClick={() => setProfileDropdownOpen(!profileDropdownOpen)}
                  className="flex items-center gap-3 rounded-md p-1.5 transition hover:bg-muted"
                >
                  <div className="grid size-9 shrink-0 place-items-center rounded-full bg-primary text-xs font-bold text-primary-foreground shadow-xs">
                    {user.initials || "MP"}
                  </div>
                  <div className="hidden min-w-0 text-left md:block">
                    <div className="truncate text-sm font-semibold">{user.name}</div>
                    <div className="truncate text-xs text-muted-foreground">{user.roleTitle}</div>
                  </div>
                  <ChevronDown size={15} className="hidden text-muted-foreground sm:block" />
                </button>

                {profileDropdownOpen && (
                  <div className="absolute right-0 top-12 z-50 w-64 rounded-lg border border-border bg-card p-3 shadow-modal animate-scale-in">
                    <div className="border-b border-border pb-3">
                      <p className="text-sm font-bold text-foreground">{user.name}</p>
                      <p className="text-xs text-muted-foreground">{user.email}</p>
                      <span className="mt-1.5 inline-block rounded-sm bg-primary-soft px-2 py-0.5 text-[11px] font-bold text-primary">
                        {user.roleTitle}
                      </span>
                    </div>

                    <div className="border-t border-border pt-2">
                      <button
                        onClick={() => {
                          logout();
                          setProfileDropdownOpen(false);
                          navigate({ to: "/login" });
                        }}
                        className="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-xs font-semibold text-danger hover:bg-danger-soft transition"
                      >
                        <LogOut size={15} /> Đăng xuất tài khoản
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </header>

        {/* Page Content */}
        <main
          id="overview"
          className="mx-auto max-w-[1540px] scroll-mt-24 p-4 sm:p-6 lg:p-8 animate-fade-in"
        >
          {children}
        </main>
      </div>
    </div>
  );
}
