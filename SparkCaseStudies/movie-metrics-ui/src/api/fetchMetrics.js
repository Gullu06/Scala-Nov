import axios from "axios";

const BASE_URL = "http://localhost:8080/api";

export const fetchMovieMetrics = async () => {
  const response = await axios.get(`${BASE_URL}/movie-metrics`);
  return response.data;
};

export const fetchGenreMetrics = async () => {
  const response = await axios.get(`${BASE_URL}/genre-metrics`);
  return response.data;
};

export const fetchDemographicsMetrics = async () => {
  const response = await axios.get(`${BASE_URL}/demographics-metrics`);
  return response.data;
};
