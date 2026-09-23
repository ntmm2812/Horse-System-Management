import { createFileRoute } from "@tanstack/react-router";
import { useState, type FormEvent } from "react";
import {
  ChevronDown,
  HeartPulse,
  Home,
  LockKeyhole,
  ShieldCheck,
  Stethoscope,
  Syringe,
} from "lucide-react";
import { toast } from "sonner";
import { AppLayout } from "@/components/layout/app-layout";
import { useStable } from "@/context/stable-context";

export const Route = createFileRoute("/vet")({
  head: () => ({
    meta: [
      { title: "Bác sĩ Thú y — Y tế & Chuồng trại | Mã Phong" },
      {
        name: "description",
        content: "Sơ đồ chuồng trại, hồ sơ bệnh án và lệnh khóa huấn luyện khẩn cấp.",
      },
    ],
  }),
  component: VetPage,
});

function VetPage() {
  const { horses, toggleHorseLock, updateHorseMedical } = useStable();
  const [selectedName, setSelectedName] = useState<string>("Hắc Phong");
  const [checked, setChecked] = useState([true, false, true]);
  const [severity, setSeverity] = useState<"Nhẹ" | "Trung bình" | "Nặng">("Trung bình");
  const [treatmentText, setTreatmentText] = useState(
    "Chườm lạnh 3 lần/ngày, nghỉ tập 7 ngày. Tái khám siêu âm vào 28/09.",
  );
  const [injuryLocation, setInjuryLocation] = useState("Chân trước trái — gân gấp");

  const currentHorse = horses.find((h) => h.name === selectedName) || horses[0];
  const isLocked = currentHorse?.isLocked ?? false;

  const handleUpdateMedical = (e: FormEvent) => {
    e.preventDefault();
    updateHorseMedical(selectedName, injuryLocation, severity, treatmentText);
  };

  return (
    <AppLayout role="vet">
      <div className="mb-6 grid gap-4 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-end">
        <div className="min-w-0">
          <p className="mb-1 text-xs font-bold uppercase tracking-[0.14em] text-secondary">
            Phân hệ Y tế
          </p>
          <h1 className="font-display text-2xl font-bold sm:text-3xl">
            Theo dõi Sức khỏe & Chấn thương
          </h1>
          <p className="mt-1 max-w-2xl text-sm text-muted-foreground">
            {horses.length} chiến mã trong chuồng •{" "}
            {horses.filter((h) => h.status !== "Khỏe").length} trường hợp cần theo dõi y tế sát.
          </p>
        </div>
        <button
          className={isLocked ? "btn-secondary" : "btn-danger"}
          onClick={() => toggleHorseLock(selectedName)}
        >
          <LockKeyhole size={17} />
          {isLocked ? "MỞ KHÓA HUẤN LUYỆN" : "KHÓA HUẤN LUYỆN KHẨN CẤP"}
        </button>
      </div>

      {isLocked && (
        <div className="mb-5 flex items-center gap-3 rounded-md border border-danger-border bg-danger-soft px-4 py-3 text-sm font-semibold text-danger animate-fade-in">
          <ShieldCheck size={19} />
          Đã khóa toàn bộ lịch tập của {selectedName}. HLV Trưởng đã được thông báo tự động.
        </div>
      )}

      <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        {/* Stall grid */}
        <section
          id="stalls"
          className="scroll-mt-24 rounded-lg border border-border bg-card shadow-card"
        >
          <div className="grid grid-cols-[minmax(0,1fr)_auto] items-start gap-4 border-b border-border px-5 py-4">
            <div className="flex min-w-0 items-start gap-3">
              <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-md bg-primary-soft text-primary">
                <Home size={17} />
              </span>
              <div>
                <h2 className="font-display text-base font-bold">Sơ đồ chuồng trại</h2>
                <p className="text-xs text-muted-foreground">
                  Chọn ô chuồng để mở và chỉnh sửa hồ sơ y tế
                </p>
              </div>
            </div>
            <div className="hidden gap-3 text-[10px] sm:flex">
              <span className="legend healthy">Khỏe</span>
              <span className="legend watch">Theo dõi</span>
              <span className="legend injured">Chấn thương</span>
            </div>
          </div>
          <div className="grid gap-3 p-5 sm:grid-cols-2">
            {horses.map((horse) => (
              <button
                key={horse.stall}
                onClick={() => setSelectedName(horse.name)}
                className={`horse-stall ${horse.tone} ${selectedName === horse.name ? "selected ring-2 ring-primary" : ""}`}
              >
                <span className="stall-no">{horse.stall}</span>
                <span className="grid size-10 place-items-center rounded-full bg-card shadow-xs">
                  <HeartPulse
                    size={18}
                    className={horse.status === "Chấn thương" ? "text-danger" : "text-primary"}
                  />
                </span>
                <span className="min-w-0 text-left">
                  <strong className="block truncate text-sm">{horse.name}</strong>
                  <small className="block truncate text-xs opacity-75">
                    {horse.status} {horse.isLocked && "• (Đã khóa)"}
                  </small>
                </span>
                <ChevronDown className="ml-auto -rotate-90 text-muted-foreground" size={15} />
              </button>
            ))}
          </div>
        </section>

        {/* Medical record card */}
        <section
          id="medical"
          className="scroll-mt-24 rounded-lg border border-border bg-card shadow-card"
        >
          <div className="grid grid-cols-[minmax(0,1fr)_auto] items-start gap-4 border-b border-border px-5 py-4">
            <div className="flex min-w-0 items-start gap-3">
              <span className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-md bg-primary-soft text-primary">
                <Stethoscope size={17} />
              </span>
              <div>
                <h2 className="font-display text-base font-bold">Hồ sơ y tế: {selectedName}</h2>
                <p className="text-xs text-muted-foreground">
                  Ô chuồng {currentHorse?.stall ?? "—"} • Nhịp tim cơ bản:{" "}
                  {currentHorse?.heartRate ?? 140} bpm
                </p>
              </div>
            </div>
          </div>
          <form className="space-y-4 p-5" onSubmit={handleUpdateMedical}>
            <label className="block">
              <span className="mb-1.5 block text-xs font-semibold">
                Vị trí chẩn đoán chấn thương
              </span>
              <select
                className="field"
                value={injuryLocation}
                onChange={(e) => setInjuryLocation(e.target.value)}
              >
                <option value="Chân trước trái — gân gấp">Chân trước trái — gân gấp</option>
                <option value="Khớp cổ chân phải">Khớp cổ chân phải</option>
                <option value="Cơ bả vai trái">Cơ bả vai trái</option>
                <option value="Xước móng sau">Xước móng sau</option>
                <option value="Không phát hiện chấn thương">
                  Không phát hiện chấn thương (Bình thường)
                </option>
              </select>
            </label>

            <label className="block">
              <span className="mb-1.5 block text-xs font-semibold">Mức độ nghiêm trọng</span>
              <div className="grid grid-cols-3 gap-2">
                {(["Nhẹ", "Trung bình", "Nặng"] as const).map((lvl) => (
                  <button
                    key={lvl}
                    type="button"
                    onClick={() => setSeverity(lvl)}
                    className={`segment ${severity === lvl ? "active font-bold" : ""}`}
                  >
                    {lvl}
                  </button>
                ))}
              </div>
            </label>

            <label className="block">
              <span className="mb-1.5 block text-xs font-semibold">Phác đồ điều trị & Y lệnh</span>
              <textarea
                className="field min-h-24"
                value={treatmentText}
                onChange={(e) => setTreatmentText(e.target.value)}
              />
            </label>

            <div className="rounded-md bg-muted p-4">
              <p className="mb-3 text-xs font-bold uppercase tracking-wide text-foreground">
                Lịch tiêm phòng & chăm sóc định kỳ
              </p>
              {[
                "Tiêm phòng cúm Equine Influenza",
                "Tẩy giun đường ruột",
                "Kiểm tra & mài răng hàm",
              ].map((x, i) => (
                <label
                  key={x}
                  className="flex cursor-pointer items-center gap-3 py-2 text-sm hover:text-primary transition"
                >
                  <input
                    type="checkbox"
                    checked={checked[i]}
                    onChange={() => {
                      const nextChecked = checked.map((v, j) => (j === i ? !v : v));
                      setChecked(nextChecked);
                      toast.info(`Đã cập nhật mục: ${x}`);
                    }}
                    className="size-4 accent-primary"
                  />
                  <span>{x}</span>
                </label>
              ))}
            </div>

            <button type="submit" className="btn-primary w-full justify-center">
              <Syringe size={17} /> Cập nhật hồ sơ y tế
            </button>
          </form>
        </section>
      </div>
    </AppLayout>
  );
}
