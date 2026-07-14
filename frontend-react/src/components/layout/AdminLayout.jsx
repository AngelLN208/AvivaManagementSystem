import { Outlet } from "react-router-dom";
import AdminSidebar from "./AdminSidebar";
import Navbar from "./Navbar";

export default function AdminLayout() {
  return (
    <div style={{ display: "flex" }}>
      <AdminSidebar />
      <main className="content w-100">
        <Navbar />
        <div className="container-fluid">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
