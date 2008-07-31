class UpdateAmendmentAddReleasedAndUpdatedDateColumn extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {

        addColumn("amendments", "released_date", "timestamp")
        addColumn("amendments", "updated_date", "timestamp")
        execute("update  amendments set updated_date= (select max(time) from audit_events ae where class_name='edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment' and ae.object_id=amendments.id  group by ae.object_id)")


    }

    void down() {
        dropColumn("amendments", "released_date")
        dropColumn("amendments", "updated_date")

    }
}