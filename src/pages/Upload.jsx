import { useState } from "react";

export default function Upload() {
  const [files, setFiles] = useState([]);

  const handleFiles = (selectedFiles) => {
    const fileArray = Array.from(selectedFiles).map((file) => ({
      file,
      preview: URL.createObjectURL(file),
      progress: 0,
    }));

    fileArray.forEach((f, i) => simulateProgress(f, i));

    setFiles((prev) => [...prev, ...fileArray]);
  };

  const simulateProgress = (fileObj, index) => {
    let progress = 0;

    const interval = setInterval(() => {
      progress += 10;

      setFiles((prev) =>
        prev.map((f) =>
          f.file.name === fileObj.file.name
            ? { ...f, progress }
            : f
        )
      );

      if (progress >= 100) clearInterval(interval);
    }, 200);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    handleFiles(e.dataTransfer.files);
  };

  const handleDelete = (index) => {
    setFiles(files.filter((_, i) => i !== index));
  };

  const getFileIcon = (name) => {
    if (name.endsWith(".pdf")) return "📄";
    if (name.match(/\.(jpg|jpeg|png)$/)) return "🖼️";
    return "📁";
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">

      <h1 className="text-2xl font-semibold mb-6">Upload Documents</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">

        {/* Drag & Drop */}
        <div
          onDrop={handleDrop}
          onDragOver={(e) => e.preventDefault()}
          className="border-2 border-dashed border-blue-300 rounded-xl p-10 flex flex-col items-center justify-center text-center bg-white hover:bg-blue-50 transition"
        >
          <div className="text-blue-500 text-4xl mb-4">⬆️</div>

          <p className="font-medium text-lg">
            Drag and drop files to upload
          </p>

          <p className="text-gray-400 my-2">or</p>

          <label className="bg-blue-500 text-white px-5 py-2 rounded-lg cursor-pointer hover:opacity-90 transition">
            Browse
            <input
              type="file"
              multiple
              className="hidden"
              onChange={(e) => handleFiles(e.target.files)}
            />
          </label>

          <p className="text-sm text-gray-400 mt-3">
            Supported files: JPG, PNG, PDF
          </p>
        </div>

        {/* File List */}
        <div className="bg-white p-6 rounded-xl shadow-sm">

          <h2 className="text-lg font-medium mb-4">
            Uploaded Files
          </h2>

          {files.length === 0 ? (
            <p className="text-gray-400">No files uploaded yet.</p>
          ) : (
            <ul className="space-y-4">
              {files.map((f, index) => (
                <li
                  key={index}
                  className="border p-3 rounded-lg"
                >
                  <div className="flex justify-between items-center mb-2">
                    <div className="flex items-center gap-2">
                      <span>{getFileIcon(f.file.name)}</span>
                      <span className="text-sm">{f.file.name}</span>
                    </div>

                    <button
                      onClick={() => handleDelete(index)}
                      className="text-red-500 hover:text-red-700"
                    >
                      ✕
                    </button>
                  </div>

                  {/* Preview */}
                  {f.file.type.startsWith("image") && (
                    <img
                      src={f.preview}
                      alt="preview"
                      className="w-full h-32 object-cover rounded mb-2"
                    />
                  )}

                  {/* Progress Bar */}
                  <div className="w-full bg-gray-200 h-2 rounded">
                    <div
                      className="bg-blue-500 h-2 rounded transition-all"
                      style={{ width: `${f.progress}%` }}
                    ></div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

      </div>
    </div>
  );
}