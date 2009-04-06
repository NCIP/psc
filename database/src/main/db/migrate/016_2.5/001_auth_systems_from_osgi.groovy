class AuthSystemsFromOsgi extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        // Back up old selection
        execute("INSERT INTO authentication_system_conf (prop, value) VALUES ('authenticationSystemPre25', (SELECT value FROM authentication_system_conf WHERE prop='authenticationSystem'))")
        
        // Only one of these (at most) will actually have an effect
        ['LOCAL', 'CAS', 'WEBSSO'].each { known ->
            execute("UPDATE authentication_system_conf SET value='edu.northwestern.bioinformatics.psc-authentication-${known.toLowerCase()}-plugin' WHERE value='${known}' AND prop='authenticationSystem'")
        }
    }
    
    void down() {
        execute("DELETE FROM authentication_system_conf WHERE prop='authenticationSystem'")
        execute("UPDATE authentication_system_conf SET prop='authenticationSystem' WHERE prop='authenticationSystemPre25'")
    }
}