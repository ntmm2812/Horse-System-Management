import { createFileRoute } from "@tanstack/react-router";
import { useMemo, useState, type FormEvent, type ReactNode } from "react";
import {
  Activity, AlertTriangle, BarChart3, Bell, CalendarDays, Check, ChevronDown,
  CircleDollarSign, ClipboardCheck, Clock3, Dumbbell, FileClock, HeartPulse,
  Home, ImagePlus, LockKeyhole, Menu, Package, Search, ShieldCheck, Sparkles,
  Stethoscope, Syringe, Trophy, UserCog, Users, Utensils, X,
} from "lucide-react";
import {
  Area, AreaChart, Bar, BarChart, CartesianGrid, Legend, Line, LineChart,
  ResponsiveContainer, Tooltip, XAxis, YAxis,
} from "recharts";
import bannerImage from "../assets/racehorse-banner.jpg";
import horseImage from "../assets/horse-profile.jpg";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Mã Phong — Quản lý Huấn luyện Ngựa đua" },
      { name: "description", content: "Trung tâm quản lý huấn luyện, sức khỏe và vận hành ngựa đua chuyên nghiệp." },
      { property: "og:title", content: "Mã Phong — Quản lý Ngựa đua" },
      { property: "og:description", content: "Theo dõi huấn luyện, y tế, chăm sóc và thành tích chiến mã trong một hệ thống." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: Dashboard,
});

type Role = "trainer" | "vet" | "groom" | "owner" | "manager";
type IconType = typeof Home;

const roles: { id: Role; label: string; sub: string; icon: IconType }[] = [
  { id: "trainer", label: "HLV Trưởng", sub: "Huấn luyện", icon: Dumbbell },
  { id: "vet", label: "Bác sĩ thú y", sub: "Y tế & chấn thương", icon: Stethoscope },
  { id: "groom", label: "Nhân viên chuồng", sub: "Chăm sóc hằng ngày", icon: ClipboardCheck },
  { id: "owner", label: "Chủ ngựa", sub: "Hồ sơ chiến mã", icon: Trophy },
  { id: "manager", label: "Quản lý CLB", sub: "Điều hành", icon: BarChart3 },
];

const trainingData = [
  { day: "T2", xichTho: 72, caviar: 66, saoMai: 61 },
  { day: "T3", xichTho: 76, caviar: 70, saoMai: 65 },
  { day: "T4", xichTho: 74, caviar: 75, saoMai: 69 },
  { day: "T5", xichTho: 82, caviar: 78, saoMai: 72 },
  { day: "T6", xichTho: 86, caviar: 81, saoMai: 76 },
  { day: "T7", xichTho: 89, caviar: 85, saoMai: 80 },
  { day: "CN", xichTho: 92, caviar: 87, saoMai: 84 },
];
const financeData = [
  { month: "T1", chiPhi: 340, doanhThu: 520 }, { month: "T2", chiPhi: 390, doanhThu: 460 },
  { month: "T3", chiPhi: 360, doanhThu: 610 }, { month: "T4", chiPhi: 430, doanhThu: 580 },
  { month: "T5", chiPhi: 410, doanhThu: 720 }, { month: "T6", chiPhi: 470, doanhThu: 790 },
];
const stableHorses = [
  { stall: "A01", name: "Xích Thố", status: "Khỏe", tone: "healthy" },
  { stall: "A02", name: "Black Caviar", status: "Theo dõi", tone: "watch" },
  { stall: "A03", name: "Sao Mai", status: "Khỏe", tone: "healthy" },
  { stall: "A04", name: "Hắc Phong", status: "Chấn thương", tone: "injured" },
  { stall: "B01", name: "Bạch Long", status: "Khỏe", tone: "healthy" },
  { stall: "B02", name: "Thiên Mã", status: "Theo dõi", tone: "watch" },
  { stall: "B03", name: "Hoàng Vũ", status: "Khỏe", tone: "healthy" },
  { stall: "B04", name: "Vệt Nắng", status: "Khỏe", tone: "healthy" },
];

function Dashboard() {
  const [role, setRole] = useState<Role>("trainer");
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const current = roles.find((item) => item.id === role) ?? roles[0];
  const View = role === "trainer" ? TrainerView : role === "vet" ? VetView : role === "groom" ? GroomView : role === "owner" ? OwnerView : ManagerView;

  return (
    <div className="min-h-screen bg-background text-foreground">
      {sidebarOpen && <button aria-label="Đóng menu" className="fixed inset-0 z-30 bg-overlay lg:hidden" onClick={() => setSidebarOpen(false)} />}
      <aside className={`fixed inset-y-0 left-0 z-40 flex w-[276px] flex-col bg-sidebar text-sidebar-foreground transition-transform duration-300 lg:translate-x-0 ${sidebarOpen ? "translate-x-0" : "-translate-x-full"}`}>
        <div className="flex h-20 items-center gap-3 border-b border-sidebar-border px-6">
          <div className="grid size-11 place-items-center rounded-md bg-sidebar-primary text-sidebar-primary-foreground"><Trophy size={22} /></div>
          <div><div className="font-display text-xl font-bold">MÃ PHONG</div><div className="text-[11px] uppercase tracking-[0.18em] text-sidebar-muted">Trung tâm huấn luyện</div></div>
          <button aria-label="Đóng menu" className="ml-auto text-sidebar-muted lg:hidden" onClick={() => setSidebarOpen(false)}><X /></button>
        </div>
        <nav className="flex-1 space-y-2 overflow-y-auto p-4" aria-label="Chuyển đổi vai trò">
          <p className="px-3 pb-2 pt-2 text-[10px] font-semibold uppercase tracking-[0.16em] text-sidebar-muted">Không gian làm việc</p>
          {roles.map((item) => {
            const Icon = item.icon;
            const active = role === item.id;
            return <button key={item.id} onClick={() => { setRole(item.id); setSidebarOpen(false); }} className={`group grid w-full grid-cols-[40px_1fr] items-center gap-2 rounded-md px-3 py-3 text-left transition ${active ? "bg-sidebar-accent text-sidebar-accent-foreground shadow-sidebar" : "text-sidebar-muted hover:bg-sidebar-hover hover:text-sidebar-foreground"}`}>
              <span className={`grid size-9 place-items-center rounded-md ${active ? "bg-sidebar-primary text-sidebar-primary-foreground" : "bg-sidebar-icon"}`}><Icon size={18} /></span>
              <span className="min-w-0"><span className="block truncate text-sm font-semibold">{item.label}</span><span className="block truncate text-[11px] opacity-65">{item.sub}</span></span>
            </button>;
          })}
        </nav>
        <div className="m-4 rounded-md border border-sidebar-border bg-sidebar-panel p-4">
          <div className="mb-2 flex items-center gap-2 text-xs font-semibold"><ShieldCheck size={15} className="text-sidebar-primary" /> Hệ thống ổn định</div>
          <p className="text-[11px] leading-relaxed text-sidebar-muted">Dữ liệu đồng bộ lúc 09:42 • 24 chiến mã đang hoạt động</p>
        </div>
      </aside>

      <div className="lg:pl-[276px]">
        <header className="sticky top-0 z-20 grid h-20 grid-cols-[auto_minmax(0,1fr)_auto] items-center gap-3 border-b border-border bg-background/95 px-4 backdrop-blur sm:px-6 lg:px-8">
          <button aria-label="Mở menu" className="grid size-10 place-items-center rounded-md border border-border lg:hidden" onClick={() => setSidebarOpen(true)}><Menu size={20} /></button>
          <div className="relative min-w-0 sm:max-w-md"><Search className="absolute left-3 top-1/2 size-17 -translate-y-1/2 text-muted-foreground" /><input className="h-11 w-full rounded-md border border-input bg-muted/60 pl-10 pr-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-ring" placeholder="Tìm chiến mã, nhân sự..." /></div>
          <div className="flex items-center gap-2 sm:gap-4">
            <button aria-label="Thông báo" className="relative grid size-10 place-items-center rounded-md border border-border hover:bg-muted"><Bell size={19} /><span className="absolute right-2 top-2 size-2 rounded-full bg-danger ring-2 ring-background" /></button>
            <div className="hidden h-8 w-px bg-border sm:block" />
            <div className="flex items-center gap-3"><div className="grid size-10 shrink-0 place-items-center rounded-full bg-primary text-sm font-bold text-primary-foreground">NA</div><div className="hidden min-w-0 md:block"><div className="text-sm font-semibold">Nguyễn Văn A</div><div className="text-xs text-muted-foreground">{current?.label}</div></div><ChevronDown size={15} className="hidden text-muted-foreground sm:block" /></div>
          </div>
        </header>
        <main className="mx-auto max-w-[1540px] p-4 sm:p-6 lg:p-8"><View /></main>
      </div>
    </div>
  );
}

function PageIntro({ eyebrow, title, description, action }: { eyebrow: string; title: string; description: string; action?: ReactNode }) {
  return <div className="mb-6 grid grid-cols-[minmax(0,1fr)_auto] items-end gap-4"><div className="min-w-0"><p className="mb-1 text-xs font-bold uppercase tracking-[0.14em] text-secondary">{eyebrow}</p><h1 className="truncate font-display text-2xl font-bold sm:text-3xl">{title}</h1><p className="mt-1 max-w-2xl text-sm text-muted-foreground">{description}</p></div>{action}</div>;
}
function Card({ children, className = "" }: { children: ReactNode; className?: string }) { return <section className={`rounded-lg border border-border bg-card shadow-card ${className}`}>{children}</section>; }
function CardHead({ title, sub, icon: Icon, action }: { title: string; sub?: string; icon?: IconType; action?: ReactNode }) { return <div className="grid grid-cols-[minmax(0,1fr)_auto] items-start gap-4 border-b border-border px-5 py-4"><div className="flex min-w-0 items-start gap-3">{Icon && <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-md bg-primary-soft text-primary"><Icon size={17} /></span>}<div className="min-w-0"><h2 className="truncate font-display text-base font-bold">{title}</h2>{sub && <p className="mt-0.5 text-xs text-muted-foreground">{sub}</p>}</div></div>{action}</div>; }
function Stat({ icon: Icon, label, value, note, tone = "primary" }: { icon: IconType; label: string; value: string; note: string; tone?: "primary" | "good" | "warn" | "danger" }) { return <Card className="horse-card p-5"><div className="flex items-start justify-between"><span className={`stat-icon ${tone}`}><Icon size={19} /></span><span className="text-[10px] font-bold uppercase tracking-wide text-muted-foreground">Hôm nay</span></div><div className="mt-5 text-2xl font-bold">{value}</div><div className="mt-1 text-xs font-semibold text-muted-foreground">{label}</div><div className={`mt-3 text-xs ${tone === "danger" ? "text-danger" : "text-success"}`}>{note}</div></Card>; }

function TrainerView() {
  const [modal, setModal] = useState(false);
  const [saved, setSaved] = useState(false);
  return <>
    <PageIntro eyebrow="Trung tâm điều phối" title="Chào buổi sáng, HLV Nguyễn Văn A" description="Tổng quan đội đua và giáo án huấn luyện ngày 22 tháng 9, 2026." action={<button className="btn-primary hidden sm:inline-flex" onClick={() => setModal(true)}><Sparkles size={17} /> Đánh giá phong độ</button>} />
    <div className="relative mb-6 min-h-[210px] overflow-hidden rounded-lg bg-hero text-hero-foreground shadow-elevated"><img src={bannerImage} width={1920} height={900} alt="Chiến mã đang thi đấu trên trường đua" className="absolute inset-0 h-full w-full object-cover" /><div className="absolute inset-0 bg-hero-overlay" /><div className="relative flex min-h-[210px] max-w-xl flex-col justify-end p-6 sm:p-8"><span className="mb-3 w-fit rounded-sm bg-hero-badge px-2.5 py-1 text-[10px] font-bold uppercase tracking-[0.16em]">Giải mùa thu • Còn 12 ngày</span><h2 className="font-display text-2xl font-bold sm:text-3xl">Xích Thố đang ở phong độ đỉnh cao</h2><p className="mt-2 text-sm text-hero-muted">Chỉ số sẵn sàng đạt 92%. Duy trì giáo án tăng tốc 1.600m trong tuần này.</p></div></div>
    <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4"><Stat icon={Activity} label="Thể lực trung bình" value="87%" note="↑ 4,2% so với tuần trước" tone="good" /><Stat icon={Dumbbell} label="Buổi tập hôm nay" value="08" note="5 buổi đã hoàn thành" /><Stat icon={HeartPulse} label="Nhịp tim trung bình" value="142 bpm" note="Trong vùng mục tiêu" tone="warn" /><Stat icon={Trophy} label="Sẵn sàng thi đấu" value="06/08" note="2 chiến mã cần theo dõi" /></div>
    <div className="grid gap-6 xl:grid-cols-[minmax(0,1.55fr)_minmax(320px,0.75fr)]">
      <Card><CardHead title="Tiến độ thể lực đội đua" sub="Điểm thể lực trong 7 ngày gần nhất" icon={Activity} action={<select className="field h-8 py-0 text-xs"><option>7 ngày</option><option>30 ngày</option></select>} /><div className="h-[310px] p-4"><ResponsiveContainer width="100%" height="100%"><LineChart data={trainingData}><CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--chart-grid)" /><XAxis dataKey="day" axisLine={false} tickLine={false} tick={{ fill: "var(--muted-foreground)", fontSize: 11 }} /><YAxis domain={[50,100]} axisLine={false} tickLine={false} tick={{ fill: "var(--muted-foreground)", fontSize: 11 }} /><Tooltip contentStyle={{ borderRadius: 6, border: "1px solid var(--border)", boxShadow: "var(--shadow-card)" }} /><Legend iconType="circle" /><Line type="monotone" dataKey="xichTho" name="Xích Thố" stroke="var(--chart-1)" strokeWidth={3} dot={{ r: 3 }} /><Line type="monotone" dataKey="caviar" name="Black Caviar" stroke="var(--chart-2)" strokeWidth={2} dot={false} /><Line type="monotone" dataKey="saoMai" name="Sao Mai" stroke="var(--chart-3)" strokeWidth={2} dot={false} /></LineChart></ResponsiveContainer></div></Card>
      <Card className="overflow-hidden"><CardHead title="Cảnh báo trực tiếp" sub="Cập nhật từ thiết bị đeo" icon={Bell} /><div className="space-y-3 p-4"><Alert horse="Hắc Phong" text="Nhịp tim 196 bpm vượt ngưỡng" time="2 phút trước" severe /><Alert horse="Black Caviar" text="Vận tốc giảm 12% ở vòng 3" time="8 phút trước" /><Alert horse="Thiên Mã" text="Nhiệt độ cơ thể 39,1°C" time="15 phút trước" /></div></Card>
      <Card className="xl:col-span-2"><CardHead title="Lịch huấn luyện hôm nay" sub="Sân chính • Thời tiết 27°C, độ ẩm 68%" icon={CalendarDays} action={<button className="btn-ghost">Xem toàn bộ</button>} /><div className="divide-y divide-border">{[
        ["06:00", "Xích Thố", "Chạy bền 2.400m", "Sân cỏ", "Hoàn thành"], ["07:15", "Black Caviar", "Tăng tốc 1.600m", "Sân cát", "Đang tập"], ["09:00", "Sao Mai", "Phản xạ xuất phát", "Cổng số 3", "Sắp tới"], ["15:30", "Bạch Long", "Hồi phục chủ động", "Hồ bơi", "Sắp tới"],
      ].map((row, i) => <div key={row[1]} className="grid grid-cols-[58px_minmax(0,1fr)] gap-3 px-5 py-4 sm:grid-cols-[70px_1fr_1fr_110px_auto] sm:items-center"><span className="text-sm font-bold text-primary">{row[0]}</span><div><p className="text-sm font-semibold">{row[1]}</p><p className="text-xs text-muted-foreground sm:hidden">{row[2]} • {row[3]}</p></div><span className="hidden text-sm sm:block">{row[2]}</span><span className="hidden text-xs text-muted-foreground sm:block">{row[3]}</span><span className={`status ${i === 0 ? "done" : i === 1 ? "active" : "neutral"}`}>{row[4]}</span></div>)}</div></Card>
    </div>
    <button className="btn-primary mt-6 w-full sm:hidden" onClick={() => setModal(true)}><Sparkles size={17} /> Đánh giá phong độ</button>
    {modal && <Modal title="Đánh giá phong độ sau buổi tập" onClose={() => setModal(false)}><form onSubmit={(e) => { e.preventDefault(); setSaved(true); setTimeout(() => setModal(false), 700); }} className="space-y-4"><Field label="Chiến mã"><select className="field"><option>Xích Thố</option><option>Black Caviar</option><option>Sao Mai</option></select></Field><Field label="Điểm phong độ (1–10)"><input className="field" type="number" min="1" max="10" defaultValue="9" /></Field><Field label="Nhận xét của huấn luyện viên"><textarea className="field min-h-28" defaultValue="Khả năng bứt tốc tốt, nhịp thở ổn định sau vòng cuối." /></Field><div className="flex justify-end gap-3"><button type="button" className="btn-secondary" onClick={() => setModal(false)}>Hủy</button><button className="btn-primary">{saved ? <><Check size={17}/>Đã lưu</> : "Lưu đánh giá"}</button></div></form></Modal>}
  </>;
}
function Alert({ horse, text, time, severe = false }: { horse: string; text: string; time: string; severe?: boolean }) { return <div className={`rounded-md border p-3 ${severe ? "alert-severe" : "bg-warning-soft border-warning-border"}`}><div className="flex gap-3"><AlertTriangle className={severe ? "text-danger" : "text-warning"} size={18} /><div><p className="text-sm font-bold">{horse}</p><p className="mt-1 text-xs leading-relaxed">{text}</p><p className="mt-2 text-[10px] text-muted-foreground">{time}</p></div></div></div>; }

function VetView() {
  const [selected, setSelected] = useState("Hắc Phong"); const [locked, setLocked] = useState(false); const [checked, setChecked] = useState([true, false, true]);
  return <><PageIntro eyebrow="Trung tâm y tế" title="Theo dõi sức khỏe & chấn thương" description="8 chiến mã trong chuồng • 2 trường hợp cần theo dõi sát." action={<button className="btn-danger" onClick={() => setLocked(!locked)}><LockKeyhole size={17} /> {locked ? "ĐÃ KHÓA HUẤN LUYỆN" : "KHÓA HUẤN LUYỆN"}</button>} />
    {locked && <div className="mb-5 flex items-center gap-3 rounded-md border border-danger-border bg-danger-soft px-4 py-3 text-sm font-semibold text-danger"><ShieldCheck size={19}/> Đã khóa toàn bộ lịch tập của {selected}. HLV Trưởng đã được thông báo.</div>}
    <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]"><Card><CardHead title="Sơ đồ chuồng trại" sub="Chọn ô chuồng để mở hồ sơ" icon={Home} action={<div className="hidden gap-3 text-[10px] sm:flex"><span className="legend healthy">Khỏe</span><span className="legend watch">Theo dõi</span><span className="legend injured">Chấn thương</span></div>} /><div className="grid gap-3 p-5 sm:grid-cols-2">{stableHorses.map((horse) => <button key={horse.stall} onClick={() => setSelected(horse.name)} className={`horse-stall ${horse.tone} ${selected === horse.name ? "selected" : ""}`}><span className="stall-no">{horse.stall}</span><span className="grid size-10 place-items-center rounded-full bg-card"><HeartPulse size={18}/></span><span className="min-w-0 text-left"><strong className="block truncate text-sm">{horse.name}</strong><small>{horse.status}</small></span><ChevronDown className="ml-auto -rotate-90" size={15}/></button>)}</div></Card>
    <Card><CardHead title={`Hồ sơ: ${selected}`} sub="Cập nhật lúc 09:38" icon={Stethoscope} /><form className="space-y-4 p-5" onSubmit={(e) => e.preventDefault()}><Field label="Vị trí chấn thương"><select className="field"><option>Chân trước trái — gân gấp</option><option>Khớp cổ chân phải</option><option>Vai trái</option><option>Không phát hiện</option></select></Field><Field label="Mức độ"><div className="grid grid-cols-3 gap-2"><button type="button" className="segment">Nhẹ</button><button type="button" className="segment active">Trung bình</button><button type="button" className="segment">Nặng</button></div></Field><Field label="Phác đồ điều trị"><textarea className="field min-h-24" defaultValue="Chườm lạnh 3 lần/ngày, nghỉ tập 7 ngày. Tái khám siêu âm vào 28/09." /></Field><div className="rounded-md bg-muted p-4"><p className="mb-3 text-xs font-bold uppercase tracking-wide">Lịch chăm sóc y tế</p>{["Tiêm phòng cúm ngựa", "Tẩy giun định kỳ", "Kiểm tra răng hàm"].map((x,i)=><label key={x} className="flex cursor-pointer items-center gap-3 py-2 text-sm"><input type="checkbox" checked={checked[i]} onChange={() => setChecked(checked.map((v,j)=>j===i?!v:v))} className="size-4 accent-primary" />{x}</label>)}</div><button className="btn-primary w-full"><Syringe size={17}/> Cập nhật hồ sơ y tế</button></form></Card></div>
  </>;
}

function GroomView() {
  const [tasks, setTasks] = useState([{t:"Cho ăn cữ sáng",s:"06:00 • Chuồng A",d:true},{t:"Vệ sinh và thay lót chuồng",s:"07:00 • Khu A–B",d:true},{t:"Tắm & chải lông Xích Thố",s:"09:30 • Khu chăm sóc",d:false},{t:"Kiểm tra móng cuối ngày",s:"16:30 • Toàn bộ",d:false}]);
  const done = tasks.filter(x=>x.d).length;
  return <><PageIntro eyebrow="Vận hành chuồng trại" title="Công việc chăm sóc hằng ngày" description="Ca sáng của Trần Minh Khoa • Thứ Ba, 22 tháng 9." action={<div className="rounded-md bg-primary-soft px-4 py-2 text-sm font-bold text-primary">{done}/{tasks.length} hoàn thành</div>} />
  <div className="grid gap-6 xl:grid-cols-[1fr_0.8fr]"><Card><CardHead title="Danh sách công việc" sub={`${Math.round(done/tasks.length*100)}% tiến độ ca sáng`} icon={ClipboardCheck}/><div className="h-1 bg-muted"><div className="h-full bg-success transition-all" style={{width:`${done/tasks.length*100}%`}} /></div><div className="divide-y divide-border">{tasks.map((task,i)=><label key={task.t} className="group flex cursor-pointer items-center gap-4 px-5 py-4 hover:bg-muted/50"><input type="checkbox" checked={task.d} onChange={()=>setTasks(tasks.map((x,j)=>j===i?{...x,d:!x.d}:x))} className="size-5 accent-primary"/><span className="grid size-9 place-items-center rounded-md bg-primary-soft text-primary"><Clock3 size={17}/></span><span><strong className={`block text-sm ${task.d ? "text-muted-foreground line-through":""}`}>{task.t}</strong><small className="text-muted-foreground">{task.s}</small></span>{task.d&&<Check className="ml-auto text-success" size={18}/>}</label>)}</div></Card>
  <Card className="overflow-hidden"><div className="relative h-36"><img src={horseImage} loading="lazy" width={1200} height={1200} alt="Xích Thố trong chuồng" className="h-full w-full object-cover object-[center_32%]"/><div className="absolute inset-0 bg-profile-overlay"/><div className="absolute bottom-4 left-5 text-hero-foreground"><p className="text-xs font-semibold text-hero-muted">Khẩu phần đang chọn</p><h2 className="font-display text-xl font-bold">Xích Thố • 518 kg</h2></div></div><div className="grid grid-cols-3 divide-x divide-border p-5 text-center"><Nutrition value="5,5 kg" label="Ngũ cốc"/><Nutrition value="8,0 kg" label="Cỏ Timothy"/><Nutrition value="120 g" label="Vitamin"/></div><div className="mx-5 mb-5 rounded-md bg-muted p-3 text-xs leading-relaxed text-muted-foreground"><Utensils size={15} className="mr-2 inline text-primary"/> Chia 3 cữ • Bổ sung điện giải sau buổi chạy tốc độ.</div></Card>
  <Card className="xl:col-span-2"><CardHead title="Báo cáo sự cố nhanh" sub="Bác sĩ thú y sẽ nhận thông báo ngay sau khi gửi" icon={AlertTriangle}/><IncidentForm/></Card></div></>;
}
function Nutrition({value,label}:{value:string;label:string}){return <div><strong className="text-lg text-primary">{value}</strong><p className="mt-1 text-[11px] text-muted-foreground">{label}</p></div>}
function IncidentForm(){const [sent,setSent]=useState(false); return <form className="grid gap-4 p-5 md:grid-cols-[0.8fr_1.4fr_auto] md:items-end" onSubmit={(e)=>{e.preventDefault();setSent(true)}}><Field label="Loại sự cố"><select className="field"><option>Ngựa bỏ ăn</option><option>Sốt</option><option>Xước móng</option><option>Hành vi bất thường</option></select></Field><Field label="Mô tả ngắn"><input className="field" placeholder="Nhập biểu hiện quan sát được..."/></Field><div className="flex gap-2"><label className="btn-secondary cursor-pointer"><ImagePlus size={17}/> Tải ảnh<input type="file" accept="image/*" className="hidden"/></label><button className="btn-primary">{sent?<><Check size={17}/>Đã gửi</>:"Gửi báo cáo"}</button></div></form>}

function OwnerView(){return <><PageIntro eyebrow="Hồ sơ chiến mã" title="Di sản của một nhà vô địch" description="Thông tin phả hệ, thể trạng và thành tích thi đấu của Xích Thố."/><Card className="mb-6 overflow-hidden"><div className="grid lg:grid-cols-[360px_1fr]"><div className="relative min-h-[360px]"><img src={horseImage} loading="lazy" width={1200} height={1200} alt="Chân dung chiến mã Xích Thố" className="absolute inset-0 h-full w-full object-cover"/><div className="absolute inset-0 bg-profile-overlay"/><span className="absolute left-5 top-5 rounded-sm bg-hero-badge px-3 py-1.5 text-[10px] font-bold uppercase tracking-widest text-hero-foreground">Thoroughbred</span></div><div className="p-6 sm:p-8"><div className="flex flex-wrap items-start justify-between gap-4"><div><p className="text-xs font-bold uppercase tracking-[0.18em] text-secondary">VN-TB-2021-0088</p><h1 className="mt-1 font-display text-4xl font-bold">XÍCH THỐ</h1><p className="mt-2 text-sm text-muted-foreground">Đực • Hạt dẻ • Sinh ngày 18/03/2021</p></div><div className="rounded-md border border-success-border bg-success-soft px-4 py-2 text-sm font-bold text-success">Đủ điều kiện thi đấu</div></div><div className="my-7 grid grid-cols-3 divide-x divide-border border-y border-border py-5"><ProfileStat value="5" label="Tuổi"/><ProfileStat value="518 kg" label="Cân nặng"/><ProfileStat value="168 cm" label="Chiều cao"/></div><h3 className="mb-4 font-display font-bold">Phả hệ ba đời</h3><div className="pedigree"><div className="pedigree-node main">Xích Thố<small>2021</small></div><div className="space-y-3"><div className="pedigree-node">Frankel<small>Cha • Galileo × Kind</small></div><div className="pedigree-node">Mộc Lan<small>Mẹ • Dubawi × Estrella</small></div></div></div><div className="mt-6 flex items-center gap-2 text-xs text-muted-foreground"><UserCog size={15}/> Chủ sở hữu: Phạm Gia Huy • HLV: Nguyễn Văn A</div></div></div></Card>
  <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]"><Card><CardHead title="Lịch sử thi đấu" sub="Mùa giải 2025–2026" icon={Trophy}/><div className="overflow-x-auto"><table><thead><tr><th>Giải đấu</th><th>Ngày</th><th>Cự ly</th><th>Hạng</th><th>Tiền thưởng</th></tr></thead><tbody>{[["Cúp Thăng Long","14/08/2026","1.800m","Hạng 1","320 tr"],["Derby Phú Thọ","02/06/2026","2.000m","Hạng 2","180 tr"],["Cúp Mùa Xuân","19/03/2026","1.600m","Hạng 1","250 tr"],["Grand Prix Đà Lạt","08/12/2025","2.400m","Hạng 3","90 tr"]].map(r=><tr key={r[0]}>{r.map((x,i)=><td key={x} className={i===3?"font-bold text-primary":""}>{x}</td>)}</tr>)}</tbody></table></div></Card><Card><CardHead title="Trạng thái sức khỏe" sub="Đồng bộ từ đội ngũ chuyên môn" icon={HeartPulse}/><div className="space-y-6 p-5"><Progress label="Sẵn sàng thi đấu" value={92}/><Progress label="Thể lực hiện tại" value={87}/><Progress label="Phục hồi cơ bắp" value={78}/><Progress label="Cân bằng dinh dưỡng" value={95}/><div className="rounded-md bg-success-soft p-4 text-xs leading-relaxed text-success"><ShieldCheck className="mr-2 inline" size={17}/>Không có chấn thương đang điều trị. Khám định kỳ tiếp theo: 28/09/2026.</div></div></Card></div></>}
function ProfileStat({value,label}:{value:string;label:string}){return <div className="text-center"><strong className="font-display text-xl">{value}</strong><p className="mt-1 text-[11px] text-muted-foreground">{label}</p></div>}
function Progress({label,value}:{label:string;value:number}){return <div><div className="mb-2 flex justify-between text-sm"><span className="font-medium">{label}</span><strong>{value}%</strong></div><div className="h-2 overflow-hidden rounded-full bg-muted"><div className="h-full rounded-full bg-primary" style={{width:`${value}%`}}/></div></div>}

function ManagerView(){const [tab,setTab]=useState<"staff"|"stock"|"log">("staff");return <><PageIntro eyebrow="Điều hành câu lạc bộ" title="Hiệu suất vận hành & tài chính" description="Tổng hợp tháng 9/2026 • Đơn vị tài chính: triệu đồng." action={<button className="btn-primary hidden sm:inline-flex"><FileClock size={17}/> Xuất báo cáo</button>}/><div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4"><Stat icon={CircleDollarSign} label="Doanh thu tháng" value="790 tr" note="↑ 9,7% so với tháng trước" tone="good"/><Stat icon={Package} label="Chi phí vận hành" value="470 tr" note="Trong ngân sách kế hoạch"/><Stat icon={Users} label="Nhân sự hoạt động" value="32" note="94% lịch trực đã phủ"/><Stat icon={Trophy} label="Tiền thưởng mùa giải" value="1,84 tỷ" note="4 chiến mã có thành tích" tone="warn"/></div><div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]"><Card><CardHead title="Tài chính 6 tháng" sub="Chi phí vận hành và doanh thu giải đấu" icon={BarChart3}/><div className="h-[315px] p-4"><ResponsiveContainer width="100%" height="100%"><BarChart data={financeData}><CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--chart-grid)"/><XAxis dataKey="month" axisLine={false} tickLine={false}/><YAxis axisLine={false} tickLine={false}/><Tooltip/><Legend/><Bar dataKey="chiPhi" name="Chi phí" fill="var(--chart-2)" radius={[3,3,0,0]}/><Bar dataKey="doanhThu" name="Doanh thu" fill="var(--chart-1)" radius={[3,3,0,0]}/></BarChart></ResponsiveContainer></div></Card><Card><CardHead title="Cơ cấu chi phí tháng 9" sub="Tổng 470 triệu đồng" icon={CircleDollarSign}/><div className="h-[315px] p-5"><ResponsiveContainer width="100%" height="100%"><AreaChart data={[{n:"Thức ăn",v:148},{n:"Y tế",v:96},{n:"Nhân sự",v:132},{n:"Bảo trì",v:58},{n:"Khác",v:36}]} layout="vertical"><XAxis type="number" hide/><YAxis dataKey="n" type="category" axisLine={false} tickLine={false} width={65} tick={{fontSize:11}}/><Tooltip/><Area type="monotone" dataKey="v" fill="var(--primary-soft)" stroke="var(--primary)" strokeWidth={2}/></AreaChart></ResponsiveContainer></div></Card>
  <Card className="xl:col-span-2"><div className="border-b border-border px-5 pt-4"><div className="flex gap-5 overflow-x-auto">{([['staff','Nhân sự',Users],['stock','Vật tư',Package],['log','Nhật ký thao tác',FileClock]] as const).map(([id,label,Icon])=><button key={id} onClick={()=>setTab(id)} className={`tab ${tab===id?'active':''}`}><Icon size={16}/>{label}</button>)}</div></div>{tab==="staff"?<StaffTable/>:tab==="stock"?<StockTable/>:<AuditLog/>}</Card></div></>}
function StaffTable(){return <div className="overflow-x-auto"><table><thead><tr><th>Nhân sự</th><th>Vị trí</th><th>Ca trực</th><th>Quyền truy cập</th><th></th></tr></thead><tbody>{[["Nguyễn Văn A","HLV Trưởng","05:30–14:00","Toàn quyền huấn luyện"],["BS. Lê Thu Hà","Bác sĩ thú y","07:00–17:00","Y tế & khóa lịch"],["Trần Minh Khoa","Nhân viên chuồng","05:00–13:00","Chăm sóc & báo cáo"],["Phạm Ngọc Lan","Kế toán","08:00–17:00","Tài chính chỉ đọc"]].map((r)=><tr key={r[0]}><td><div className="flex items-center gap-3"><span className="grid size-8 place-items-center rounded-full bg-primary-soft text-xs font-bold text-primary">{r[0]?.trim().slice(-1) ?? "N"}</span><strong>{r[0]}</strong></div></td><td>{r[1]}</td><td>{r[2]}</td><td><span className="status neutral">{r[3]}</span></td><td><button className="btn-ghost">Phân quyền</button></td></tr>)}</tbody></table></div>}
function StockTable(){return <div className="grid gap-3 p-5 md:grid-cols-3">{[["Cỏ Timothy","680 kg","Đủ 18 ngày"],["Ngũ cốc hiệu suất","245 kg","Đủ 12 ngày"],["Bộ băng bảo vệ","14 bộ","Cần nhập thêm"]].map((r,i)=><div key={r[0]} className="rounded-md border border-border p-4"><Package className="mb-4 text-primary"/><strong className="block">{r[0]}</strong><span className="mt-1 block text-2xl font-bold">{r[1]}</span><small className={i===2?"text-danger":"text-muted-foreground"}>{r[2]}</small></div>)}</div>}
function AuditLog(){return <div className="divide-y divide-border">{[["09:42","BS. Lê Thu Hà","Cập nhật hồ sơ y tế của Hắc Phong"],["09:31","Nguyễn Văn A","Điều chỉnh giáo án Black Caviar"],["08:55","Trần Minh Khoa","Hoàn thành vệ sinh khu chuồng A"],["08:10","Phạm Ngọc Lan","Duyệt phiếu nhập vật tư #VT-0922"]].map(r=><div key={r[0]} className="grid grid-cols-[52px_1fr] gap-3 px-5 py-4 sm:grid-cols-[70px_170px_1fr]"><span className="text-xs font-bold text-primary">{r[0]}</span><strong className="text-sm">{r[1]}</strong><span className="text-xs text-muted-foreground">{r[2]}</span></div>)}</div>}

function Field({label,children}:{label:string;children:ReactNode}){return <label className="block"><span className="mb-1.5 block text-xs font-semibold">{label}</span>{children}</label>}
function Modal({title,onClose,children}:{title:string;onClose:()=>void;children:ReactNode}){return <div className="fixed inset-0 z-50 grid place-items-center bg-overlay p-4" role="dialog" aria-modal="true"><div className="w-full max-w-lg animate-scale-in rounded-lg bg-card shadow-modal"><div className="flex items-center justify-between border-b border-border px-5 py-4"><h2 className="font-display text-lg font-bold">{title}</h2><button aria-label="Đóng" className="grid size-8 place-items-center rounded-md hover:bg-muted" onClick={onClose}><X size={18}/></button></div><div className="p-5">{children}</div></div></div>}
