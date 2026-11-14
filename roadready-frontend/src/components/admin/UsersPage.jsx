// src/components/admin/UsersPage.jsx
import { useDispatch } from "react-redux";
import { deleteAdminUser } from "./store/action/AdminAction";

function UsersPage({ users, setError, setSuccess }) {
    const dispatch = useDispatch();

    const handleDeleteUser = async (userId) => {
        if (!window.confirm("Are you sure you want to deactivate and delete this user?")) return;
        dispatch(deleteAdminUser(userId));
        setSuccess("User account deleted.");
    };

    return (
        <section className="dashboard-section">
            <h3 style={{ marginBottom: '20px' }}>System Users</h3>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                    <tr style={{ borderBottom: '2px solid #E6EBE3' }}>
                        <th style={{ textAlign: 'left', padding: '10px' }}>User ID</th>
                        <th style={{ textAlign: 'left', padding: '10px' }}>Username</th>
                        <th style={{ textAlign: 'left', padding: '10px' }}>Role</th>
                        <th style={{ textAlign: 'left', padding: '10px' }}>Action</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map(u => (
                        <tr key={u.userId} style={{ borderBottom: '1px solid #E6EBE3' }}>
                            <td style={{ padding: '10px' }}>#{u.userId}</td>
                            <td style={{ padding: '10px', fontWeight: 'bold' }}>{u.username}</td>
                            <td style={{ padding: '10px' }}>
                                <span style={{ padding: '4px 8px', borderRadius: '12px', fontSize: '12px', backgroundColor: u.role === 'ADMIN' ? '#F8D7DA' : u.role === 'RENTAL_AGENT' ? '#FFF3CD' : '#D4EDDA', color: u.role === 'ADMIN' ? '#721C24' : u.role === 'RENTAL_AGENT' ? '#856404' : '#155724' }}>
                                    {u.role}
                                </span>
                            </td>
                            <td style={{ padding: '10px' }}>
                                <button onClick={() => handleDeleteUser(u.userId)} style={{ color: '#D9534F', background: 'transparent', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>Deactivate</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </section>
    );
}
export default UsersPage;