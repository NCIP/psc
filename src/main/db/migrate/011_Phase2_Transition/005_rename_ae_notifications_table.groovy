class CreateNotificationTables extends edu.northwestern.bioinformatics.bering.Migration {

    void up() {

        addColumn("ae_notifications", "action_required", "boolean")
        addColumn("ae_notifications", "message", "string")
        addColumn("ae_notifications", "title", "string")


        execute('update ae_notifications  set message=(select aes.description from adverse_events aes where aes.id=ae_notifications.adverse_event_id)')

        execute("update ae_notifications  set title=(select aes.detection_date from adverse_events aes where aes.id=ae_notifications.adverse_event_id)")

        execute("update  notifications  set title='Serious Adverse Event on ' || title")

        execute("update ae_notifications  set action_required='TRUE'")

        execute("ALTER TABLE ae_notifications RENAME TO notifications")

         if (databaseMatches('oracle')) {
                     execute("RENAME seq_ae_notifications_id TO seq_notifications_id");
          }

          if (databaseMatches('postgresql')) {

            execute("ALTER TABLE ae_notifications_id_seq RENAME TO notifications_id_seq");
        }

      dropColumn("notifications", "adverse_event_id")


    }

    void down() {
           dropColumn("ae_notifications", "action_required")
           dropColumn("ae_notifications", "message")
           dropColumn("ae_notifications", "title")

        execute("ALTER TABLE notifications RENAME TO ae_notifications")

             if (databaseMatches('oracle')) {
                         execute("RENAME seq_notifications_id TO seq_ae_notifications_id");
              }

              if (databaseMatches('postgresql')) {

                execute("ALTER TABLE notifications_id_seq  RENAME TO ae_notifications_id_seq ");
            }
        }

}