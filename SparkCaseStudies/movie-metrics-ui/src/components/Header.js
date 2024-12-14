import React from "react";
import { AppBar, Toolbar, Typography } from "@mui/material";

const Header = () => {
  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6">Movie Metrics Dashboard</Typography>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
