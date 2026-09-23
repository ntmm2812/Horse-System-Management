import { ROLE_PAGES } from "@/lib/roles";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useState, useEffect, type FormEvent } from "react";
import {
  Trophy,
  Dumbbell,
  Stethoscope,
  ClipboardCheck,
  BarChart3,
  Mail,
  Lock,
  User as UserIcon,
  Phone,
  Sparkles,
  ArrowRight,
  ShieldCheck,
  Eye,
  EyeOff,
  CheckCircle2,
  Compass,
} from "lucide-react";
import { useAuth, DEMO_USERS, type Role } from "@/context/auth-context";

export const Route = createFileRoute("/login")({
  head: () => ({
    meta: [
      { title: "Đăng nhập — Cổng 5 Phân hệ Vai trò | Mã Phong" },
      {
        name: "description",
        content: "Đăng nhập và truy cập 5 phân hệ quản lý huấn luyện ngựa đua Mã Phong.",
      },
    ],
  }),
  component: LoginPage,
});

const ROLE_INFO: Array<{
  role: Role;
  label: string;
  name: string;
  route: string;
  desc: string;
  badge: string;
  icon: typeof Dumbbell;
}> = [
  {
    role: "trainer",
    label: "HLV Trưởng",
    name: DEMO_USERS.trainer.name,
    route: "/trainer",
    desc: "Giáo án thể lực, chỉ số phong độ & điều phối buổi tập",
    badge: "Huấn luyện",
    icon: Dumbbell,
  },
  {
    role: "vet",
    label: "Bác sĩ Thú y",
    name: DEMO_USERS.vet.name,
    route: "/vet",
    desc: "Bệnh án y tế, phác đồ điều trị & khóa huấn luyện khẩn",
    badge: "Y tế & Chuồng",
    icon: Stethoscope,
  },
  {
    role: "groom",
    label: "Nhân viên Chuồng",
    name: DEMO_USERS.groom.name,
    route: "/groom",
    desc: "Checklist ca trực, khẩu phần dinh dưỡng & sự cố",
    badge: "Chăm sóc",
    icon: ClipboardCheck,
  },
  {
    role: "owner",
    label: "Chủ Ngựa",
    name: DEMO_USERS.owner.name,
    route: "/owner",
    desc: "Hồ sơ chiến mã, phả hệ 3 đời & giải thưởng mùa giải",
    badge: "Chủ sở hữu",
    icon: Trophy,
  },
  {
    role: "manager",
    label: "Quản lý CLB",
    name: DEMO_USERS.manager.name,
    route: "/manager",
    desc: "Doanh thu & chi phí 6 tháng, phân quyền & kho vật tư",
    badge: "Điều hành",
    icon: BarChart3,
  },
];

function LoginPage() {
  const { user, isAuthenticated, login, register, isLoading } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState<"login" | "register">("login");
  const [showPassword, setShowPassword] = useState(false);

  // Auto redirect if already logged in
  useEffect(() => {
    if (isAuthenticated && user?.role) {
      navigate({ to: ROLE_PAGES[user.role].path, replace: true });
    }
  }, [isAuthenticated, user, navigate]);

  // Login form state
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");

  // Register form state
  const [regName, setRegName] = useState("");
  const [regEmail, setRegEmail] = useState("");
  const [regPhone, setRegPhone] = useState("");
  const [regPassword, setRegPassword] = useState("");
  const [regRole, setRegRole] = useState<Role>("trainer");

  const handleLoginSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!loginEmail) return;
    const loggedUser = await login(loginEmail, loginPassword);
    if (loggedUser) {
      navigate({ to: ROLE_PAGES[loggedUser.role].path });
    }
  };

  const handleRegisterSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!regName || !regEmail || !regPassword) return;
    const newUser = await register(regName, regEmail, regPassword, regRole, regPhone);
    if (newUser) {
      navigate({ to: ROLE_PAGES[newUser.role].path });
    }
  };

  const handleQuickLogin = async (role: Role) => {
    const demo = DEMO_USERS[role];
    const loggedUser = await login(demo.email, "demo123", role);
    if (loggedUser) {
      navigate({ to: ROLE_PAGES[role].path });
    }
  };

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col justify-center px-4 py-8 sm:px-6 lg:px-8 relative overflow-hidden">
      {/* Background ambient lighting accents */}
      <div className="absolute -top-40 -left-40 size-[32rem] rounded-full bg-primary/10 blur-[100px] pointer-events-none" />
      <div className="absolute -bottom-40 -right-40 size-[32rem] rounded-full bg-secondary/15 blur-[100px] pointer-events-none" />

      <div className="mx-auto w-full max-w-5xl">
        {/* Top Brand Header */}
        <div className="mb-8 text-center">
          <div className="inline-flex items-center gap-3 rounded-full border border-border/80 bg-card/60 px-4 py-1.5 shadow-xs backdrop-blur-sm mb-4">
            <span className="flex size-2 rounded-full bg-success animate-pulse" />
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-widest">
              Hệ thống Vận hành Chuyên nghiệp
            </span>
          </div>
          <div className="flex items-center justify-center gap-3">
            <div className="grid size-12 place-items-center rounded-xl bg-sidebar text-sidebar-primary shadow-sm">
              <Trophy size={24} />
            </div>
            <h1 className="font-display text-3xl font-extrabold tracking-tight sm:text-4xl">
              MÃ PHONG
            </h1>
          </div>
          <p className="mt-2 text-sm text-muted-foreground max-w-xl mx-auto">
            Hệ thống Quản lý Huấn luyện Ngựa đua Chuyên nghiệp — Phân tách 5 phân hệ chuyên biệt
            theo từng vai trò nghiệp vụ.
          </p>
        </div>

        <div className="grid gap-8 lg:grid-cols-[1.1fr_0.9fr] items-start">
          {/* Left Column: 5 Dedicated Role Cards with 1-Click Access */}
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="font-display text-lg font-bold flex items-center gap-2">
                  <Compass size={18} className="text-primary" /> Phân hệ 5 Vai trò Nghiệp vụ
                </h2>
                <p className="text-xs text-muted-foreground">
                  Chọn vai trò để đăng nhập trực tiếp vào trang chuyên môn tương ứng:
                </p>
              </div>
            </div>

            <div className="grid gap-3">
              {ROLE_INFO.map((item) => {
                const Icon = item.icon;
                return (
                  <button
                    key={item.role}
                    type="button"
                    onClick={() => handleQuickLogin(item.role)}
                    disabled={isLoading}
                    className="group flex items-center gap-3.5 rounded-xl border border-border bg-card/80 p-3.5 text-left transition hover:border-primary hover:bg-card hover:shadow-card hover:-translate-y-0.5 active:translate-y-0"
                  >
                    <div className="grid size-11 shrink-0 place-items-center rounded-lg bg-primary/10 text-primary transition group-hover:bg-primary group-hover:text-primary-foreground">
                      <Icon size={22} />
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <strong className="text-sm font-bold text-foreground">{item.label}</strong>
                        <span className="rounded-sm bg-muted px-2 py-0.5 text-[10px] font-semibold text-muted-foreground">
                          {item.badge}
                        </span>
                        <span className="ml-auto text-xs text-primary font-medium opacity-0 group-hover:opacity-100 transition hidden sm:inline-flex items-center gap-1">
                          Vào trang <ArrowRight size={13} />
                        </span>
                      </div>
                      <p className="mt-0.5 truncate text-xs text-muted-foreground">{item.desc}</p>
                      <div className="mt-1 flex items-center gap-1.5 text-[11px] text-muted-foreground/80">
                        <CheckCircle2 size={12} className="text-success" />
                        <span>Mẫu: {item.name}</span>
                        <span className="opacity-50">•</span>
                        <span className="font-mono text-[10px] text-primary">{item.route}</span>
                      </div>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Right Column: Standard Login / Register Card */}
          <div className="overflow-hidden rounded-2xl border border-border bg-card shadow-modal">
            {/* Card Header with Tabs */}
            <div className="border-b border-border bg-muted/40 p-4">
              <div className="flex rounded-lg bg-background p-1 border border-border">
                <button
                  type="button"
                  onClick={() => setTab("login")}
                  className={`flex-1 rounded-md py-2 text-center text-xs font-bold transition ${
                    tab === "login"
                      ? "bg-primary text-primary-foreground shadow-xs"
                      : "text-muted-foreground hover:text-foreground"
                  }`}
                >
                  Đăng nhập tài khoản
                </button>
                <button
                  type="button"
                  onClick={() => setTab("register")}
                  className={`flex-1 rounded-md py-2 text-center text-xs font-bold transition ${
                    tab === "register"
                      ? "bg-primary text-primary-foreground shadow-xs"
                      : "text-muted-foreground hover:text-foreground"
                  }`}
                >
                  Đăng ký vai trò mới
                </button>
              </div>
            </div>

            <div className="p-6">
              {tab === "login" ? (
                <form onSubmit={handleLoginSubmit} className="space-y-4">
                  <div>
                    <label className="mb-1.5 block text-xs font-semibold text-foreground">
                      Email đăng nhập
                    </label>
                    <div className="relative">
                      <Mail className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                      <input
                        type="email"
                        required
                        value={loginEmail}
                        onChange={(e) => setLoginEmail(e.target.value)}
                        placeholder="trainer@maphong.vn"
                        className="h-11 w-full rounded-md border border-input bg-muted/40 pl-10 pr-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-ring"
                      />
                    </div>
                  </div>

                  <div>
                    <div className="mb-1.5 flex items-center justify-between">
                      <label className="text-xs font-semibold text-foreground">Mật khẩu</label>
                      <span className="text-[11px] text-muted-foreground">
                        Demo: mật khẩu bất kỳ
                      </span>
                    </div>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                      <input
                        type={showPassword ? "text" : "password"}
                        required
                        value={loginPassword}
                        onChange={(e) => setLoginPassword(e.target.value)}
                        placeholder="••••••••"
                        className="h-11 w-full rounded-md border border-input bg-muted/40 pl-10 pr-10 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-ring"
                      />
                      <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                      >
                        {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                      </button>
                    </div>
                  </div>

                  <button
                    type="submit"
                    disabled={isLoading}
                    className="btn-primary w-full justify-center py-2.5 text-sm shadow-sm"
                  >
                    <ArrowRight size={17} /> Đăng nhập & Vào trang vai trò
                  </button>

                  <div className="rounded-lg bg-muted/30 p-3 text-[11px] leading-relaxed text-muted-foreground border border-border/60">
                    <strong className="text-foreground">Lưu ý:</strong> Sau khi đăng nhập, hệ thống
                    sẽ tự động chuyển hướng đến trang tương ứng với quyền hạn của bạn.
                  </div>
                </form>
              ) : (
                <form onSubmit={handleRegisterSubmit} className="space-y-4">
                  <div>
                    <label className="mb-1.5 block text-xs font-semibold text-foreground">
                      Họ và tên
                    </label>
                    <div className="relative">
                      <UserIcon className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                      <input
                        type="text"
                        required
                        value={regName}
                        onChange={(e) => setRegName(e.target.value)}
                        placeholder="Ví dụ: Hoàng Tuấn Kiệt"
                        className="h-10 w-full rounded-md border border-input bg-muted/40 pl-10 pr-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-ring"
                      />
                    </div>
                  </div>

                  <div className="grid gap-3 sm:grid-cols-2">
                    <div>
                      <label className="mb-1.5 block text-xs font-semibold text-foreground">
                        Email
                      </label>
                      <div className="relative">
                        <Mail className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                        <input
                          type="email"
                          required
                          value={regEmail}
                          onChange={(e) => setRegEmail(e.target.value)}
                          placeholder="kiet@maphong.vn"
                          className="h-10 w-full rounded-md border border-input bg-muted/40 pl-10 pr-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-ring"
                        />
                      </div>
                    </div>
                    <div>
                      <label className="mb-1.5 block text-xs font-semibold text-foreground">
                        Số điện thoại
                      </label>
                      <div className="relative">
                        <Phone className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                        <input
                          type="tel"
                          value={regPhone}
                          onChange={(e) => setRegPhone(e.target.value)}
                          placeholder="0988 123 456"
                          className="h-10 w-full rounded-md border border-input bg-muted/40 pl-10 pr-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-ring"
                        />
                      </div>
                    </div>
                  </div>

                  <div>
                    <label className="mb-1.5 block text-xs font-semibold text-foreground">
                      Phân hệ vai trò tiếp nhận
                    </label>
                    <select
                      value={regRole}
                      onChange={(e) => setRegRole(e.target.value as Role)}
                      className="field h-10"
                    >
                      <option value="trainer">Huấn luyện viên Trưởng (/trainer)</option>
                      <option value="vet">Bác sĩ Thú y (/vet)</option>
                      <option value="groom">Nhân viên Chăm sóc (/groom)</option>
                      <option value="owner">Chủ sở hữu Ngựa đua (/owner)</option>
                      <option value="manager">Quản lý & Điều hành CLB (/manager)</option>
                    </select>
                  </div>

                  <div>
                    <label className="mb-1.5 block text-xs font-semibold text-foreground">
                      Mật khẩu
                    </label>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                      <input
                        type="password"
                        required
                        value={regPassword}
                        onChange={(e) => setRegPassword(e.target.value)}
                        placeholder="••••••••"
                        className="h-10 w-full rounded-md border border-input bg-muted/40 pl-10 pr-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-ring"
                      />
                    </div>
                  </div>

                  <div className="flex items-center gap-2 rounded-md bg-muted/40 p-2.5 text-xs text-muted-foreground">
                    <ShieldCheck size={16} className="text-success shrink-0" />
                    Tài khoản mới sẽ tự động đăng nhập và đưa tới phân hệ tương ứng.
                  </div>

                  <button
                    type="submit"
                    disabled={isLoading}
                    className="btn-primary w-full justify-center py-2.5 text-sm shadow-sm"
                  >
                    <Sparkles size={16} /> Đăng ký & Bắt đầu làm việc
                  </button>
                </form>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
