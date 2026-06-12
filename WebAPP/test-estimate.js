const axios = require('axios');
axios.post('http://localhost:8080/RideBook/bookings/estimate-price', {
  pickupLat: 10.8231,
  pickupLng: 106.6297,
  dropoffLat: 10.8331,
  dropoffLng: 106.6397,
  promotionCodes: ["102021", "SUMMER20"]
}).then(r => console.log(JSON.stringify(r.data, null, 2))).catch(e => console.error(e.message));
