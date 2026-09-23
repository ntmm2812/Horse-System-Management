import { createFileRoute } from "@tanstack/react-router";
import { useState, useRef, type FormEvent, type ChangeEvent } from "react";
import {
  AlertTriangle,
  Check,
  ClipboardCheck,
  Clock3,
  ImagePlus,
  Plus,
  Utensils,
  X,
} from "lucide-react";
import { toast } from "sonner";
import horseImage from "@/assets/horse-profile.jpg";
import { AppLayout } from "@/components/layout/app-layout";
import { useStable } from "@/context/stable-context";

export const Route = createFileRoute("/groom")({
  head: () => ({
    meta: [
      { title: "Nhân viên Chuồng — Vận hành & Chăm sóc | Mã Phong" },
      {
        name: "description",
        content: "Checklist công việc chăm sóc hằng ngày, khẩu phần ăn và báo cáo sự cố.",
      },
    ],
  }),
  component: GroomPage,
});

function GroomPage() {
  const { horses, addAuditLog, addNotification } = useStable();
  const [tasks, setTasks] = useState([
    { id: "t1", t: "Cho ăn cữ sáng (Ngũ cốc + cỏ khô)", s: "06:00 • Dãy chuồng A", d: true },
    { id: "t2", t: "Vệ sinh và thay đệm lót chuồng", s: "07:00 • Khu chuồng A–B", d: true },
    { id: "t3", t: "Tắm & chải lông chiến mã Xích Thố", s: "09:30 • Khu chăm sóc", d: false },
    { id: "t4", t: "Kiểm tra móng và vệ sinh cuối ngày", s: "16:30 • Toàn bộ 8 ô", d: false },
  ]);
  const [addTaskModal, setAddTaskModal] = useState(false);
  const [newTaskTitle, setNewTaskTitle] = useState("");
  const [newTaskSub, setNewTaskSub] = useState("14:00 • Khu A");

  // Incident form states
  const [incidentHorse, setIncidentHorse] = useState("Xích Thố");
  const [incidentType, setIncidentType] = useState("Ngựa bỏ ăn");
  const [incidentDesc, setIncidentDesc] = useState("");
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [isSent, setIsSent] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const doneCount = tasks.filter((x) => x.d).length;
  const progressPercent = Math.round((doneCount / tasks.length) * 100);

  const toggleTask = (id: string) => {
    setTasks((prev) =>
      prev.map((item) => {
        if (item.id === id) {
          const next = !item.d;
          if (next) toast.success(`Đã hoàn thành: ${item.t}`);
          return { ...item, d: next };
        }
        return item;
      }),
    );
  };

  const handleAddNewTask = (e: FormEvent) => {
    e.preventDefault();
    if (!newTaskTitle) return;
    const newTask = {
      id: `task-${Date.now()}`,
      t: newTaskTitle,
      s: newTaskSub,
      d: false,
    };
    setTasks((prev) => [...prev, newTask]);
    toast.success("Đã thêm công việc mới vào ca trực!");
    setNewTaskTitle("");
    setAddTaskModal(false);
  };

  const handleImageChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        setImagePreview(reader.result as string);
        toast.info("Đã đính kèm ảnh chụp hiện trường.");
      };
      reader.readAsDataURL(file);
    }
  };

  const handleIncidentSubmit = (e: FormEvent) => {
    e.preventDefault();
    setIsSent(true);
    addAuditLog(
      `Nhân viên chuồng báo cáo sự cố: ${incidentHorse} - ${incidentType} (${incidentDesc || "Không có mô tả"})`,
    );
    addNotification(
      `Báo cáo sự cố: ${incidentHorse}`,
      `Phát hiện: ${incidentType} - ${incidentDesc || "Cần bác sĩ kiểm tra"}`,
      "warn",
    );
    toast.success(`Đã gửi báo cáo sự cố của ${incidentHorse} tới Bác sĩ thú y!`);
    setTimeout(() => {
      setIsSent(false);
      setIncidentDesc("");
      setImagePreview(null);
    }, 2000);
  };

  return (
    <AppLayout role="groom">
      <div className="mb-6 grid gap-4 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-end">
        <div className="min-w-0">
          <p className="mb-1 text-xs font-bold uppercase tracking-[0.14em] text-secondary">
            Phân hệ Chăm sóc
          </p>
          <h1 className="font-display text-2xl font-bold sm:text-3xl">
            Vận hành Chuồng trại & Dinh dưỡng
          </h1>
          <p className="mt-1 max-w-2xl text-sm text-muted-foreground">
            Checklist công việc chăm sóc hằng ngày • Thứ Ba, 22 tháng 9.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button onClick={() => setAddTaskModal(true)} className="btn-secondary text-xs py-2">
            <Plus size={15} /> Thêm việc
          </button>
          <div className="rounded-md bg-primary-soft px-4 py-2 text-sm font-bold text-primary">
            {doneCount}/{tasks.length} hoàn thành
          </div>
        </div>
      </div>

      <div className="grid gap-6 xl:grid-cols-[1fr_0.8fr]">
        {/* Daily Tasks checklist */}
        <section
          id="tasks"
          className="scroll-mt-24 rounded-lg border border-border bg-card shadow-card"
        >
          <div className="grid grid-cols-[minmax(0,1fr)_auto] items-start gap-4 border-b border-border px-5 py-4">
            <div className="flex min-w-0 items-start gap-3">
              <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-md bg-primary-soft text-primary">
                <ClipboardCheck size={17} />
              </span>
              <div>
                <h2 className="font-display text-base font-bold">Danh sách công việc ca trực</h2>
                <p className="text-xs text-muted-foreground">
                  {progressPercent}% tiến độ hoàn thành
                </p>
              </div>
            </div>
          </div>
          <div className="h-1.5 bg-muted">
            <div
              className="h-full bg-success transition-all duration-300"
              style={{ width: `${progressPercent}%` }}
            />
          </div>
          <div className="divide-y divide-border">
            {tasks.map((task) => (
              <label
                key={task.id}
                className="group flex cursor-pointer items-center gap-4 px-5 py-4 hover:bg-muted/50 transition"
              >
                <input
                  type="checkbox"
                  checked={task.d}
                  onChange={() => toggleTask(task.id)}
                  className="size-5 accent-primary"
                />
                <span className="grid size-9 place-items-center rounded-md bg-primary-soft text-primary">
                  <Clock3 size={17} />
                </span>
                <span className="flex-1 min-w-0">
                  <strong
                    className={`block text-sm ${task.d ? "text-muted-foreground line-through" : ""}`}
                  >
                    {task.t}
                  </strong>
                  <small className="text-muted-foreground">{task.s}</small>
                </span>
                {task.d && <Check className="text-success" size={18} />}
              </label>
            ))}
          </div>
        </section>

        {/* Nutrition Card */}
        <section
          id="nutrition"
          className="scroll-mt-24 rounded-lg border border-border bg-card shadow-card overflow-hidden"
        >
          <div className="relative h-36">
            <img
              src={horseImage}
              loading="lazy"
              width={1200}
              height={1200}
              alt="Xích Thố trong chuồng"
              className="h-full w-full object-cover object-[center_32%]"
            />
            <div className="absolute inset-0 bg-profile-overlay" />
            <div className="absolute bottom-4 left-5 text-hero-foreground">
              <p className="text-xs font-semibold text-hero-muted">Khẩu phần ăn chiến mã</p>
              <h2 className="font-display text-xl font-bold">Xích Thố • 518 kg</h2>
            </div>
          </div>
          <div className="grid grid-cols-3 divide-x divide-border p-5 text-center">
            <div>
              <strong className="text-lg text-primary">5,5 kg</strong>
              <p className="mt-1 text-[11px] text-muted-foreground">Ngũ cốc dinh dưỡng</p>
            </div>
            <div>
              <strong className="text-lg text-primary">8,0 kg</strong>
              <p className="mt-1 text-[11px] text-muted-foreground">Cỏ khô Timothy</p>
            </div>
            <div>
              <strong className="text-lg text-primary">120 g</strong>
              <p className="mt-1 text-[11px] text-muted-foreground">Vitamin & khoáng</p>
            </div>
          </div>
          <div className="mx-5 mb-5 rounded-md bg-muted p-3 text-xs leading-relaxed text-muted-foreground">
            <Utensils size={15} className="mr-2 inline text-primary" />
            Chia 3 cữ: 06:00, 12:00, 18:00 • Bổ sung điện giải ngay sau buổi chạy tốc độ.
          </div>
        </section>

        {/* Incident Reporting Form */}
        <section
          id="incidents"
          className="scroll-mt-24 rounded-lg border border-border bg-card shadow-card xl:col-span-2"
        >
          <div className="grid grid-cols-[minmax(0,1fr)_auto] items-start gap-4 border-b border-border px-5 py-4">
            <div className="flex min-w-0 items-start gap-3">
              <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-md bg-primary-soft text-primary">
                <AlertTriangle size={17} />
              </span>
              <div>
                <h2 className="font-display text-base font-bold">
                  Báo cáo sự cố chuồng trại nhanh
                </h2>
                <p className="text-xs text-muted-foreground">
                  Bác sĩ thú y và HLV sẽ nhận thông báo tức thời ngay khi gửi
                </p>
              </div>
            </div>
          </div>
          <form onSubmit={handleIncidentSubmit} className="p-5 space-y-4">
            <div className="grid gap-4 md:grid-cols-3">
              <label className="block">
                <span className="mb-1.5 block text-xs font-semibold">Chiến mã liên quan</span>
                <select
                  value={incidentHorse}
                  onChange={(e) => setIncidentHorse(e.target.value)}
                  className="field"
                >
                  {horses.map((h) => (
                    <option key={h.stall} value={h.name}>
                      {h.name} ({h.stall})
                    </option>
                  ))}
                </select>
              </label>

              <label className="block">
                <span className="mb-1.5 block text-xs font-semibold">Loại sự cố</span>
                <select
                  value={incidentType}
                  onChange={(e) => setIncidentType(e.target.value)}
                  className="field"
                >
                  <option value="Ngựa bỏ ăn">Ngựa bỏ ăn / ăn ít</option>
                  <option value="Sốt hoặc thân nhiệt cao">Sốt hoặc thân nhiệt cao</option>
                  <option value="Xước móng / đau chân">Xước móng / đau chân</option>
                  <option value="Hành vi bồn chồn bất thường">Hành vi bồn chồn bất thường</option>
                  <option value="Hư hỏng máng ăn / cửa chuồng">Hư hỏng máng ăn / cửa chuồng</option>
                </select>
              </label>

              <label className="block">
                <span className="mb-1.5 block text-xs font-semibold">Mô tả chi tiết</span>
                <input
                  className="field"
                  value={incidentDesc}
                  onChange={(e) => setIncidentDesc(e.target.value)}
                  placeholder="Nhập triệu chứng quan sát được..."
                />
              </label>
            </div>

            {imagePreview && (
              <div className="relative w-fit">
                <img
                  src={imagePreview}
                  alt="Preview"
                  className="h-24 w-36 rounded-md object-cover border border-border"
                />
                <button
                  type="button"
                  onClick={() => setImagePreview(null)}
                  className="absolute -right-2 -top-2 grid size-5 place-items-center rounded-full bg-danger text-white text-xs"
                >
                  ×
                </button>
              </div>
            )}

            <div className="flex items-center justify-between pt-2">
              <label className="btn-secondary cursor-pointer">
                <ImagePlus size={17} /> Tải ảnh hiện trường
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  className="hidden"
                  onChange={handleImageChange}
                />
              </label>
              <button type="submit" className="btn-primary">
                {isSent ? (
                  <>
                    <Check size={17} /> Đã gửi thành công
                  </>
                ) : (
                  "Gửi báo cáo khẩn"
                )}
              </button>
            </div>
          </form>
        </section>
      </div>

      {/* Add task modal */}
      {addTaskModal && (
        <div
          className="fixed inset-0 z-50 grid place-items-center bg-overlay/80 p-4 backdrop-blur-xs animate-fade-in"
          role="dialog"
        >
          <div className="w-full max-w-lg overflow-hidden rounded-lg border border-border bg-card shadow-modal animate-scale-in">
            <div className="flex items-center justify-between border-b border-border px-5 py-4">
              <h2 className="font-display text-lg font-bold">Thêm công việc vào ca chăm sóc</h2>
              <button
                className="grid size-8 place-items-center rounded-md hover:bg-muted"
                onClick={() => setAddTaskModal(false)}
              >
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleAddNewTask} className="space-y-4 p-5">
              <label className="block">
                <span className="mb-1.5 block text-xs font-semibold">Tên công việc</span>
                <input
                  required
                  value={newTaskTitle}
                  onChange={(e) => setNewTaskTitle(e.target.value)}
                  placeholder="Ví dụ: Bổ sung muối khoáng ô B02"
                  className="field"
                />
              </label>
              <label className="block">
                <span className="mb-1.5 block text-xs font-semibold">Thời gian & Địa điểm</span>
                <input
                  required
                  value={newTaskSub}
                  onChange={(e) => setNewTaskSub(e.target.value)}
                  placeholder="11:30 • Dãy chuồng B"
                  className="field"
                />
              </label>
              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={() => setAddTaskModal(false)}
                >
                  Hủy
                </button>
                <button type="submit" className="btn-primary">
                  Thêm vào checklist
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AppLayout>
  );
}
