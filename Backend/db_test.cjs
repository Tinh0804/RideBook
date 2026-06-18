const { Client } = require('pg');
const fs = require('fs');
const yaml = require('js-yaml');

try {
    const fileContents = fs.readFileSync('src/main/resources/application.yaml', 'utf8');
    const data = yaml.load(fileContents);
    const dbUrl = data.spring.datasource.url;
    const dbUser = data.spring.datasource.username;
    const dbPass = data.spring.datasource.password;
    
    console.log("DB Config:", dbUrl, dbUser, dbPass ? "***" : "none");
    
    // Quick script to connect via psql or pg package. Let's just output it so I know what it is.
} catch (e) {
    console.log(e);
}
