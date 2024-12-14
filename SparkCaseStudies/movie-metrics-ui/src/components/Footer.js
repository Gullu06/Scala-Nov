import React from "react";
import { Box, Typography } from "@mui/material";

const Footer = () => {
  return (
    <Box component="footer" sx={{ textAlign: "center", py: 2, backgroundColor: "#f5f5f5" }}>
      <Typography variant="body2">© 2024 Movie Metrics Dashboard</Typography>
    </Box>
  );
};

export default Footer;
