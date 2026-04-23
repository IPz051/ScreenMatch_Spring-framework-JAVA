package br.com.alura.Screenmatch.model;

import java.text.Normalizer;

public enum CategoriaEnum {
        ACAO("Action" , "Ação"), 
        ROMANCE("Romance" , "Romance"), 
        COMEDIA("Comedy" , "Comédia"), 
        DRAMA("Drama" , "Drama"), 
        CRIME("Crime" , "Crime");

        private String categoriaOMDB;
        private String categoriaEmPortugues;

        CategoriaEnum(String categoriaOMDB, String categoriaEmPortugues) {
            this.categoriaOMDB = categoriaOMDB;
            this.categoriaEmPortugues = categoriaEmPortugues;
        }



        private static String normalizar(String text) {
            if (text == null) {
                return "";
            }
            String semAcento = Normalizer.normalize(text, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}+", "");
            return semAcento.trim().toLowerCase();
        }

        public static CategoriaEnum fromString(String text) {
            String normalizado = normalizar(text);
            for (CategoriaEnum categoria : CategoriaEnum.values()){
                if (normalizar(categoria.categoriaOMDB).equals(normalizado)){
                    return categoria;
                }
            }
            throw new IllegalArgumentException("Nenhuma categoria encontrada para a string fornecida: " + text);
        }
        
        public static CategoriaEnum fromPortugues(String text) {
            String normalizado = normalizar(text);
            for (CategoriaEnum categoria : CategoriaEnum.values()){
                if (normalizar(categoria.categoriaEmPortugues).equals(normalizado)){
                    return categoria;
                }
            }
            throw new IllegalArgumentException("Nenhuma categoria encontrada para a string fornecida: " + text);
        }

        @Override
        public String toString() {
            return categoriaEmPortugues;
        }

}
