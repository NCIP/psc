class CreatePscCreateNewActivtySource extends edu.northwestern.bioinformatics.bering.Migration {

    void up() {
        insert( 'sources',
            [ name:'PSC - Manual Activity Creation',
              version:0
            ]
        )

    }

    void down() {
        execute("DELETE from sources where name = 'PSC - Manual Activity Creation'")
    }
}

