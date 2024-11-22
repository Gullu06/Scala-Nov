import axios from "axios";

const API_BASE = "http://localhost:9000"; // Correct base URL

export default axios.create({
    baseURL: API_BASE,
    headers: {
        "Content-Type": "application/json",
    },
});
