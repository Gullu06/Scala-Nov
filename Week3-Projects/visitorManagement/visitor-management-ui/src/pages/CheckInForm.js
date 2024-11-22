import React, { useState } from "react";
import axios from "../services/visitorService";
import "./CheckInForm.css";

function CheckInForm() {
    console.log("CheckInForm component rendered!");

    const [formData, setFormData] = useState({
        name: "",
        contact_no: "",
        address: "",
        host_name: "",
        host_email: "",
        visitor_email: "",
        purpose: "",
        block: "",
        id_proof: "",
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        console.log(`Field changed: ${name} = ${value}`);
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        console.log("Form submitted!");
        console.log("Submitting form data:", formData);

        try {
            const response = await axios.post("/checkin", formData);
            // Call backend API
            console.log("Response from server:", response.data);

            alert("Visitor checked in successfully!");
            // Reset form after successful submission
            setFormData({
                name: "",
                contact_no: "",
                address: "",
                host_name: "",
                host_email: "",
                visitor_email: "",
                purpose: "",
                block: "",
                id_proof: "",
            });
        } catch (error) {
            if (error.response) {
                console.error("Response error data:", error.response.data);
                console.error("Response status:", error.response.status);
                alert(`Failed to check in visitor: ${error.response.data.message || "Error"}`);
            } else if (error.request) {
                console.error("No response received:", error.request);
                alert("Failed to reach the server. Please try again.");
            } else {
                console.error("Request setup error:", error.message);
                alert("An unexpected error occurred. Please try again.");
            }
        }
    };

    return (
        <div className="form-wrapper">
            <h2>Visitor Check-In</h2>
            <form onSubmit={handleSubmit}>
                <input
                    name="name"
                    type="text"
                    placeholder="Visitor Name"
                    value={formData.name}
                    onChange={handleChange}
                    required
                />
                <input
                    name="contact_no"
                    type="text"
                    placeholder="Contact Number"
                    value={formData.contact_no}
                    onChange={handleChange}
                    required
                />
                <textarea
                    name="address"
                    placeholder="Address"
                    value={formData.address}
                    onChange={handleChange}
                    required
                />
                <input
                    name="host_name"
                    type="text"
                    placeholder="Host Name"
                    value={formData.host_name}
                    onChange={handleChange}
                    required
                />
                <input
                    name="host_email"
                    type="email"
                    placeholder="Host Email"
                    value={formData.host_email}
                    onChange={handleChange}
                    required
                />
                <input
                    name="visitor_email"
                    type="email"
                    placeholder="Visitor Email"
                    value={formData.visitor_email}
                    onChange={handleChange}
                    required
                />
                <textarea
                    name="purpose"
                    placeholder="Purpose of Visit"
                    value={formData.purpose}
                    onChange={handleChange}
                    required
                />
                <input
                    name="block"
                    type="text"
                    placeholder="Block"
                    value={formData.block}
                    onChange={handleChange}
                    required
                />
                <input
                    name="id_proof"
                    type="text"
                    placeholder="Base64ID"
                    value={formData.id_proof}
                    onChange={handleChange}
                    required
                />
                <button type="submit">Check In</button>
            </form>
        </div>
    );
}

export default CheckInForm;
