const { Client } = require('pg');
const client = new Client({
  connectionString: 'postgresql://neondb_owner:npg_tx7fq4apowLB@ep-shy-fog-ao30otbi-pooler.c-2.ap-southeast-1.aws.neon.tech/ridebook?sslmode=require'
});
client.connect()
  .then(() => client.query("SELECT driver_id, account_id FROM driver WHERE driver_id = 'ac6554cc-ff25-4104-8575-9ba7ff2d3031' OR account_id = 'ac6554cc-ff25-4104-8575-9ba7ff2d3031';"))
  .then(res => { console.log("Rows:", res.rows); client.end(); })
  .catch(err => { console.error("Error:", err); client.end(); });
