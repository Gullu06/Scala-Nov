import React from "react";
import Header from "./components/Header";
import Footer from "./components/Footer";
import MetricsDisplay from "./components/MetricsDisplay";
import { Box } from "@mui/material";

const App = () => {
  return (
    <Box>
      <Header />
      <MetricsDisplay />
      <Footer />
    </Box>
  );
};

export default App;
