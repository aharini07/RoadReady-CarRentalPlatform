// src/components/admin/FleetPage.jsx
import { useState } from "react";
import { useDispatch } from "react-redux";
import { addAdminCar, updateAdminCar, deleteAdminCar } from "./store/action/AdminAction";

function FleetPage({ cars, setError, setSuccess }) {
    const dispatch = useDispatch();
    const [editingCarId, setEditingCarId] = useState(null);
    const [carForm, setCarForm] = useState({
        carName: "", brand: "", model: "", fuelType: "Petrol", seatingCapacity: 5, dailyRate: "", status: "AVAILABLE", licensePlate: ""
    });

    const handleSaveCar = async (e) => {
        e.preventDefault();
        setError(""); setSuccess("");
        
        if (editingCarId) {
            dispatch(updateAdminCar(editingCarId, carForm));
            setSuccess("Car updated successfully!");
        } else {
            dispatch(addAdminCar(carForm));
            setSuccess("Car added to fleet successfully!");
        }
        resetCarForm();
    };

    const handleDeleteCar = async (carId) => {
        if (!window.confirm("Are you sure you want to remove this car from the fleet?")) return;
        dispatch(deleteAdminCar(carId));
        setSuccess("Car removed from fleet.");
    };

    const startEditCar = (car) => {
        setEditingCarId(car.carId);
        setCarForm({
            carName: car.carName, brand: car.brand, model: car.model, 
            fuelType: car.fuelType, seatingCapacity: car.seatingCapacity, 
            dailyRate: car.dailyRate, status: car.status, licensePlate: car.licensePlate || ""
        });
    };

    const resetCarForm = () => {
        setEditingCarId(null);
        setCarForm({ carName: "", brand: "", model: "", fuelType: "Petrol", seatingCapacity: 5, dailyRate: "", status: "AVAILABLE", licensePlate: "" });
    };

    return (
        <div style={{ display: 'flex', gap: '20px', alignItems: 'flex-start' }}>
            <section className="dashboard-section" style={{ flex: 1, border: editingCarId ? '2px solid #16845c' : '1px solid #E6EBE3' }}>
                <h3 style={{ marginBottom: '20px', color: editingCarId ? '#16845c' : '#20352A' }}>
                    {editingCarId ? "Edit Car Details" : "Add New Car"}
                </h3>
                <form onSubmit={handleSaveCar} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <input className="form-input" placeholder="Car Name" required value={carForm.carName} onChange={e => setCarForm({...carForm, carName: e.target.value})} />
                    <input className="form-input" placeholder="Brand" required value={carForm.brand} onChange={e => setCarForm({...carForm, brand: e.target.value})} />
                    <input className="form-input" placeholder="Model" required value={carForm.model} onChange={e => setCarForm({...carForm, model: e.target.value})} />
                    <input className="form-input" placeholder="License Plate" required value={carForm.licensePlate} onChange={e => setCarForm({...carForm, licensePlate: e.target.value})} />
                    <select className="form-input" value={carForm.fuelType} onChange={e => setCarForm({...carForm, fuelType: e.target.value})}>
                        <option value="Petrol">Petrol</option>
                        <option value="Diesel">Diesel</option>
                        <option value="Electric">Electric</option>
                    </select>
                    <input type="number" className="form-input" placeholder="Seating Capacity" required value={carForm.seatingCapacity} onChange={e => setCarForm({...carForm, seatingCapacity: parseInt(e.target.value)})} />
                    <input type="number" className="form-input" placeholder="Daily Rate (₹)" required value={carForm.dailyRate} onChange={e => setCarForm({...carForm, dailyRate: parseFloat(e.target.value)})} />
                    
                    {editingCarId && (
                        <select className="form-input" value={carForm.status} onChange={e => setCarForm({...carForm, status: e.target.value})}>
                            <option value="AVAILABLE">AVAILABLE</option>
                            <option value="MAINTENANCE">MAINTENANCE</option>
                            <option value="RENTED">RENTED</option>
                        </select>
                    )}

                    <button type="submit" className="btn-primary-green">{editingCarId ? "Update Car" : "Add to Fleet"}</button>
                    {editingCarId && (
                        <button type="button" onClick={resetCarForm} style={{ background: 'transparent', border: 'none', color: '#D9534F', textDecoration: 'underline', cursor: 'pointer' }}>
                            Cancel Edit
                        </button>
                    )}
                </form>
            </section>

            <section className="dashboard-section" style={{ flex: 2 }}>
                <h3 style={{ marginBottom: '20px' }}>Current Fleet</h3>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                        <tr style={{ borderBottom: '2px solid #E6EBE3' }}>
                            <th style={{ textAlign: 'left', padding: '10px' }}>Car</th>
                            <th style={{ textAlign: 'left', padding: '10px' }}>Plate</th>
                            <th style={{ textAlign: 'left', padding: '10px' }}>Rate/Day</th>
                            <th style={{ textAlign: 'left', padding: '10px' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {cars.map(car => (
                            <tr key={car.carId} style={{ borderBottom: '1px solid #E6EBE3' }}>
                                <td style={{ padding: '10px' }}><strong>{car.carName}</strong> <br/><small>{car.brand}</small></td>
                                <td style={{ padding: '10px', fontWeight: 'bold' }}>{car.licensePlate || "N/A"}</td>
                                <td style={{ padding: '10px' }}>₹{car.dailyRate}</td>
                                <td style={{ padding: '10px', display: 'flex', gap: '10px' }}>
                                    <button onClick={() => startEditCar(car)} style={{ color: '#397A45', background: 'transparent', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>Edit</button>
                                    <button onClick={() => handleDeleteCar(car.carId)} style={{ color: '#D9534F', background: 'transparent', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>Delete</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </section>
        </div>
    );
}
export default FleetPage;