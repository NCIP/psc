class ModifyStudy extends edu.northwestern.bioinformatics.bering.Migration {

void up() {

             addColumn("studies", "load_status", "integer", nullable: true , defaultValue:"1");

             execute("update studies set load_status = 1");

             setNullable("studies", "load_status", false);

              addColumn("studies", "long_title", "string", nullable: false);

               execute("update studies set long_title = name");

               setNullable("studies", "long_title", false);



        if (databaseMatches('hsqldb')) {
            execute('ALTER TABLE studies ALTER COLUMN protocol_authority_id RENAME TO assigned_identifier')
        } else {
            execute('ALTER TABLE studies RENAME COLUMN protocol_authority_id TO assigned_identifier')
        }

 }

void down() {


            if (databaseMatches('hsqldb')) {
            execute('ALTER TABLE studies ALTER COLUMN assigned_identifier  RENAME TO protocol_authority_id ')
        } else {
            execute('ALTER TABLE studies RENAME COLUMN assigned_identifier  TO protocol_authority_id')
        }
             dropColumn("studies","long_title")
             dropColumn("studies","load_status")

     }



 }

