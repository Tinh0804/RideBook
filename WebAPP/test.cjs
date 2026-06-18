const axios = require('axios');
(async () => {
    try {
        const res = await axios.post('http://localhost:8080/RideBook/auth/login', {
            userName: "admin", 
            passWord: "admin",
            roleName: "ADMIN"
        });
        const token = res.data.result?.token || res.data.token;
        console.log("Token acquired:", !!token);
        
        const driversRes = await axios.get('http://localhost:8080/RideBook/admin/drivers?page=0&size=1000', {
            headers: { Authorization: `Bearer ${token}` }
        });
        console.log("Drivers status:", driversRes.status);
        console.log("Drivers data preview:", JSON.stringify(driversRes.data).substring(0, 500));
    } catch(e) {
        console.error("Error status:", e.response?.status);
        console.error("Error data:", e.response?.data);
    }
})();
