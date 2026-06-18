const axios = require('axios');
(async () => {
    try {
        const res = await axios.post('http://localhost:8080/RideBook/auth/login', {
            userName: "admin", 
            passWord: "admin",
            roleName: "ADMIN"
        });
        const token = res.data.result?.token || res.data.token;
        
        const driversRes = await axios.get('http://localhost:8080/RideBook/admin/drivers?page=0&size=10', {
            headers: { Authorization: `Bearer ${token}` }
        });
        const drivers = driversRes.data.result?.content || [];
        if (drivers.length > 0) {
            console.log("Driver from list:", JSON.stringify(drivers[0], null, 2));
            const detailRes = await axios.get(`http://localhost:8080/RideBook/admin/drivers/${drivers[0].driverId}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            console.log("Driver detail:", JSON.stringify(detailRes.data.result, null, 2));
        } else {
            console.log("No drivers found");
        }
    } catch(e) {
        console.error("Error:", e.message);
    }
})();
