import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import CheckInForm from "./pages/CheckInForm";
import CheckOut from "./pages/CheckOut";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/checkin" element={<CheckInForm />} />
                <Route path="/checkout" element={<CheckOut />} />
            </Routes>
        </Router>
    );
}

export default App;
