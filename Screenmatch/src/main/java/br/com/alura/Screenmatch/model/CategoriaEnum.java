package br.com.alura.Screenmatch.model;

public enum CategoriaEnum {
        ACAO("Action"), 
        ROMANCE("Romance"), 
        COMEDIA("Comedy"), 
        DRAMA("Drama"), 
        CRIME("Crime");

        private String categoriaOMDB;

        CategoriaEnum(String categoriaOMDB) {
            this.categoriaOMDB = categoriaOMDB;
        }

        public static CategoriaEnum fromString(String text) {
            for (CategoriaEnum categoria : CategoriaEnum.values()){
                if (categoria.categoriaOMDB.equalsIgnoreCase(text)){
                    return categoria;
                }
            }
            throw new IllegalArgumentException("Nenhuma categoria encontrada para a string fornecida: " + text);
        }
}
