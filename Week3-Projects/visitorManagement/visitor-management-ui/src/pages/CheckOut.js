import React, { useState } from "react";

function CheckOutForm() {
    const [visitorEmail, setVisitorEmail] = useState("");
    const [message, setMessage] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage(""); // Clear any previous messages

        console.log("Form submitted with email:", visitorEmail);

        if (!visitorEmail) {
            setMessage("Please enter a visitor email.");
            return;
        }

        try {
            console.log("Sending request to backend...");
            const response = await fetch("http://localhost:9000/checkout", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ visitorEmail }),
            });

            console.log("Response received:", response);

            if (response.ok) {
                const responseData = await response.json();
                console.log("Success response data:", responseData);
                setMessage("Visitor checked out successfully!");
            } else {
                const errorData = await response.json();
                console.error("Error response data:", errorData);
                setMessage(errorData.error || "Failed to check out the visitor.");
            }
        } catch (error) {
            console.error("Error during checkout:", error);
            setMessage("An error occurred. Please try again.");
        }
    };

    return (
        <div style={containerStyle}>
            <form onSubmit={handleSubmit} style={formStyle}>
                <h2>Visitor Check-Out</h2>
                <label htmlFor="visitorEmail" style={labelStyle}>
                    Visitor Email:
                </label>
                <input
                    type="email"
                    id="visitorEmail"
                    name="visitorEmail"
                    value={visitorEmail}
                    onChange={(e) => setVisitorEmail(e.target.value)}
                    placeholder="Enter visitor's email"
                    style={inputStyle}
                    required
                />
                <button type="submit" style={buttonStyle}>
                    Check Out
                </button>
            </form>
            {message && <p style={messageStyle}>{message}</p>}
        </div>
    );
}

const containerStyle = {
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    marginTop: "50px",
};

const formStyle = {
    display: "flex",
    flexDirection: "column",
    maxWidth: "400px",
    padding: "20px",
    border: "1px solid #ccc",
    borderRadius: "5px",
    boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)",
};

const labelStyle = {
    marginBottom: "10px",
    fontWeight: "bold",
};

const inputStyle = {
    marginBottom: "20px",
    padding: "10px",
    fontSize: "16px",
    border: "1px solid #ccc",
    borderRadius: "5px",
};

const buttonStyle = {
    padding: "10px",
    fontSize: "16px",
    fontWeight: "bold",
    color: "#fff",
    backgroundColor: "#007BFF",
    border: "none",
    borderRadius: "5px",
    cursor: "pointer",
};

const messageStyle = {
    marginTop: "20px",
    fontSize: "16px",
    color: "green",
};

export default CheckOutForm;
