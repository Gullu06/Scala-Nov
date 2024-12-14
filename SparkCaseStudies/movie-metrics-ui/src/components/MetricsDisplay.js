import React, { useState, useEffect } from "react";
import { Button, Typography, Box, CircularProgress } from "@mui/material";
import { fetchMovieMetrics, fetchGenreMetrics, fetchDemographicsMetrics } from "../api/fetchMetrics";

const MetricsDisplay = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleFetch = async (fetchFunction) => {
    setLoading(true);
    setError(null);
    try {
      const result = await fetchFunction();
      setData(result);
    } catch (err) {
      setError("Failed to fetch data");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        Fetch Metrics
      </Typography>
      <Box sx={{ display: "flex", gap: 2, mb: 3 }}>
        <Button variant="contained" onClick={() => handleFetch(fetchMovieMetrics)}>
          Movie Metrics
        </Button>
        <Button variant="contained" onClick={() => handleFetch(fetchGenreMetrics)}>
          Genre Metrics
        </Button>
        <Button variant="contained" onClick={() => handleFetch(fetchDemographicsMetrics)}>
          Demographics Metrics
        </Button>
      </Box>
      <Box sx={{ mt: 3 }}>
        {loading && <CircularProgress />}
        {error && <Typography color="error">{error}</Typography>}
        {data && (
          <Typography
            variant="body1"
            component="pre"
            sx={{
              backgroundColor: "#f5f5f5",
              padding: 2,
              borderRadius: 1,
              overflow: "auto",
              maxHeight: 400,
            }}
          >
            {data}
          </Typography>
        )}
      </Box>
    </Box>
  );
};

export default MetricsDisplay;
