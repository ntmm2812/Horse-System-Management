import { useState, type FormEvent } from "react";

/**
 * Khung form nhập liệu: Xác nhận hoàn thành công việc chăm sóc
 * Dùng cho phân hệ Groom (Role: Groom)
 * Tương ứng API Backend: POST /api/groom/tasks/{id}/complete
 * DTO Backend: com.horsemanagement.dto.groom.CareTaskDto
 */
export interface CareTaskCompleteData {
  taskId: number;
  note?: string;
}

interface CareTaskFormProps {
  onCompleteTask?: (data: CareTaskCompleteData) => void | Promise<void>;
  isLoading?: boolean;
}

export function CareTaskForm({ onCompleteTask, isLoading = false }: CareTaskFormProps) {
  const [taskId, setTaskId] = useState<number>(1);
  const [note, setNote] = useState<string>("");
  const [message, setMessage] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (onCompleteTask) {
      await onCompleteTask({ taskId, note });
      setMessage(`Đã xác nhận hoàn thành nhiệm vụ #${taskId}!`);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="p-6 bg-white dark:bg-zinc-900 rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm space-y-4 max-w-xl">
      <div>
        <h3 className="text-lg font-bold text-zinc-900 dark:text-zinc-100">Khung nhập liệu: Hoàn thành nhiệm vụ Groom</h3>
        <p className="text-sm text-zinc-500">Groom xác nhận đã thực hiện xong các công việc cho ăn, tắm rửa, chải lông cho ngựa</p>
      </div>

      {message && (
        <div className="p-3 text-sm text-emerald-700 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-lg">
          {message}
        </div>
      )}

      <div>
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
          Mã nhiệm vụ chăm sóc (TaskID) <span className="text-red-500">*</span>
        </label>
        <input
          type="number"
          min={1}
          required
          value={taskId}
          onChange={(e) => setTaskId(parseInt(e.target.value) || 1)}
          placeholder="Mã task lấy từ danh sách GET /api/groom/tasks"
          className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-zinc-700 dark:text-zinc-300 mb-1">
          Ghi chú thực hiện (tùy chọn)
        </label>
        <textarea
          rows={2}
          value={note}
          onChange={(e) => setNote(e.target.value)}
          placeholder="Ví dụ: Đã cho ăn đủ định lượng cỏ, ngựa ăn tốt, uống nước bình thường..."
          className="w-full px-3 py-2 border rounded-lg dark:bg-zinc-800 dark:border-zinc-700 text-sm outline-none"
        />
      </div>

      <div className="pt-2 flex justify-end">
        <button
          type="submit"
          disabled={isLoading}
          className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-medium text-sm rounded-lg transition-colors disabled:opacity-50"
        >
          {isLoading ? "Đang xử lý..." : "Xác nhận hoàn thành"}
        </button>
      </div>
    </form>
  );
}
