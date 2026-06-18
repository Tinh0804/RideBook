const axios = require('axios');
(async () => {
    try {
        // Fetch vehicle types
        const vtRes = await axios.get('http://localhost:8080/RideBook/admin/vehicle-types');
        const vtypes = vtRes.data.result || [];
        const vtypeId = vtypes.length > 0 ? vtypes[0].vehicleTypeId : "1"; // Assuming 1 if none found

        const payload = {
            driverName: "Nguyen Van A",
            birthDate: "1990-01-01",
            citizenId: "012345678912",
            drivingLicense: "GPLX-12345",
            phone: "0366900823",
            email: "driver_test@gmail.com",
            licensePlate: "29A-12345",
            vehicleName: "Toyota Vios",
            gender: "MALE",
            address: "Hanoi",
            area: "Hanoi",
            vehicleTypeId: vtypeId,
            password: "12345"
        };
        const res = await axios.post('http://localhost:8080/RideBook/drivers/register', payload);
        console.log("Success:", res.data);
    } catch(e) {
        console.error("Error creating driver:", e.response?.data || e.message);
    }
})();
