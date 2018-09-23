package nos.nakisha.org.miMuseoNOS;

import android.provider.BaseColumns;

/**
 * Created by nakis on 14/12/2017.
 */

public final class Estructura_BD {
    //NOTA:
    //el final viene sugerido del snippet de Android Developers

    //mi constructor vacío y privado para evitar ser instanciado accidentalmente
    private Estructura_BD() {
    }

    /* Inner class que define el contenido de la tabla */
    public static class Parametros implements BaseColumns {
        public static final String TABLE_NAME = "parametros";
        public static final String NOMBRE_COLUMNA_1 = "major" ;
        public static final String NOMBRE_COLUMNA_2 = "ancho" ;
        public static final String NOMBRE_COLUMNA_3 = "largo";
        public static final String NOMBRE_COLUMNA_4 = "angulo";
        public static final String NOMBRE_COLUMNA_5 = "sala";
        public static final String NOMBRE_COLUMNA_6 = "ocx";
        public static final String NOMBRE_COLUMNA_7 = "ocy";
        public static final String NOMBRE_COLUMNA_8 = "media";
    }
    //MÉTODOS PARA CREAR Y BORRAR LA TABLA DE PARAMETROS
    public static final String SQL_CREATE_TABLA_PARAMETROS =
            "CREATE TABLE " + Parametros.TABLE_NAME + " (" +
                    Parametros._ID + " INTEGER PRIMARY KEY," +
                    Parametros.NOMBRE_COLUMNA_1 + " TEXT," +
                    Parametros.NOMBRE_COLUMNA_2 + " TEXT," +
                    Parametros.NOMBRE_COLUMNA_3 + " TEXT," +
                    Parametros.NOMBRE_COLUMNA_4 + " TEXT," +
                    Parametros.NOMBRE_COLUMNA_5 + " TEXT," +
                    Parametros.NOMBRE_COLUMNA_6 + " TEXT," +
                    Parametros.NOMBRE_COLUMNA_7 + " TEXT," +
                    Parametros.NOMBRE_COLUMNA_8 + " TEXT)" ;

    public static final String SQL_DELETE_TABLA_PARAMETROS =
            "DROP TABLE IF EXISTS " + Parametros.TABLE_NAME;

    //TABLA BALIZAS
    public static class Balizas implements BaseColumns{
        public static final String TABLE_NAME="balizas";
        public static final String NOMBRE_COLUMNA_1 ="major" ;
        public static final String NOMBRE_COLUMNA_2="id_estimote";
        public static final String NOMBRE_COLUMNA_3="posX";
        public static final String NOMBRE_COLUMNA_4="posY";
        public static final String NOMBRE_COLUMNA_5="posZ";
        public static final String NOMBRE_COLUMNA_6="sala";

    }
    //MÉTODOS PARA CREAR Y BORRAR LA TABLA DE BALIZAS

        public static final String SQL_CREATE_TABLA_BALIZAS=
                "CREATE TABLE "+ Balizas.TABLE_NAME + " ("+
                        Balizas._ID + " INTEGER PRIMARY KEY," +
                        Balizas.NOMBRE_COLUMNA_1+" TEXT,"+
                        Balizas.NOMBRE_COLUMNA_2+" TEXT,"+
                        Balizas.NOMBRE_COLUMNA_3+" TEXT,"+
                        Balizas.NOMBRE_COLUMNA_4+" TEXT,"+
                        Balizas.NOMBRE_COLUMNA_5+" TEXT,"+
                        Balizas.NOMBRE_COLUMNA_6+" TEXT)";

        public static final String SQL_DELETE_TABLA_BALIZAS=
                "DROP TABLE IF EXISTS "+ Balizas.TABLE_NAME;


    //TABLA CUADROS
    public static class Cuadros implements BaseColumns{
        public static final String TABLE_NAME="cuadros";
        public static final String NOMBRE_COLUMNA_1 ="major" ;
        public static final String NOMBRE_COLUMNA_2="url";
        public static final String NOMBRE_COLUMNA_3="nombre";
        public static final String NOMBRE_COLUMNA_4="posX";
        public static final String NOMBRE_COLUMNA_5="posY";
        public static final String NOMBRE_COLUMNA_6="posZ";
        public static final String NOMBRE_COLUMNA_7="sala";
        public static final String NOMBRE_COLUMNA_8="media";
        public static final String NOMBRE_COLUMNA_9="foto";

    }

    //MÉTODOS PARA CREAR Y BORRAR LA TABLA DE CUADROS

    public static final String SQL_CREATE_TABLA_CUADROS=
            "CREATE TABLE "+ Cuadros.TABLE_NAME + " ("+
                    Cuadros._ID + " INTEGER PRIMARY KEY," +
                    Cuadros.NOMBRE_COLUMNA_1+" TEXT,"+
                    Cuadros.NOMBRE_COLUMNA_2+" TEXT,"+
                    Cuadros.NOMBRE_COLUMNA_3+" TEXT,"+
                    Cuadros.NOMBRE_COLUMNA_4+" TEXT,"+
                    Cuadros.NOMBRE_COLUMNA_5+" TEXT,"+
                    Cuadros.NOMBRE_COLUMNA_6+" TEXT,"+
                    Cuadros.NOMBRE_COLUMNA_7+" TEXT,"+
                    Cuadros.NOMBRE_COLUMNA_8+" TEXT,"+
                    Cuadros.NOMBRE_COLUMNA_9+" TEXT)";

    public static final String SQL_DELETE_TABLA_CUADROS =
            "DROP TABLE IF EXISTS "+ Cuadros.TABLE_NAME;


    //TABLA SIGMATABLE
    public static class SigmaTable implements BaseColumns {
        public static final String TABLE_NAME = "sigmatable";
        public static final String NOMBRE_COLUMNA_1 ="sigma" ;
        public static final String NOMBRE_COLUMNA_2 ="major" ;
        public static final String NOMBRE_COLUMNA_3 ="p" ;
        public static final String NOMBRE_COLUMNA_4 ="sala" ;

    }
    //MÉTODOS PARA CREAR Y BORRAR LA TABLA DE SIGMATABLE
    public static final String SQL_CREATE_TABLA_SIGMATABLE =
            "CREATE TABLE " + SigmaTable.TABLE_NAME + " (" +
                    SigmaTable._ID + " INTEGER PRIMARY KEY," +
                    SigmaTable.NOMBRE_COLUMNA_1 + " TEXT,"+
                    SigmaTable.NOMBRE_COLUMNA_2 + " TEXT,"+
                    SigmaTable.NOMBRE_COLUMNA_3 + " TEXT,"+
                    SigmaTable.NOMBRE_COLUMNA_4 + " TEXT)";

    public static final String SQL_DELETE_TABLA_SIGMATABLE =
            "DROP TABLE IF EXISTS " + SigmaTable.TABLE_NAME;


}

