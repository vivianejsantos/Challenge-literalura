package com.alura.literalura.principal;

import com.alura.literalura.model.Autor;
import com.alura.literalura.model.DadosAutor;
import com.alura.literalura.model.DadosLivro;
import com.alura.literalura.model.Livro;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LivroRepository;
import com.alura.literalura.service.ConsumoApi;
import com.alura.literalura.service.DadosConverter;

import java.util.DoubleSummaryStatistics;
import java.util.Scanner;

public class Principal {
    private Scanner sc = new Scanner(System.in);
    private ConsumoApi requisicao = new ConsumoApi();
    private AutorRepository repositorioAutor;
    private LivroRepository repositorioLivro;
    //private List<Livro> livro = new ArrayList<>();
    private DadosConverter conversor = new DadosConverter();
    private final String ADDRESS = "https://gutendex.com/books?search=";

    public Principal(AutorRepository repositorioAutor, LivroRepository repositorioLivro) {
        this.repositorioAutor = repositorioAutor;
        this.repositorioLivro = repositorioLivro;
    }

    public void principal(){

        String menu = """
        
                ========================================    M E N U   =============================================================
                1 - BUSCAR LIVRO PELO TITULO
                2 - LISTAR LIVROS REGISTRADOS
                3 - LISTAR AUTORES REGISTRADOS
                4 - LISTAR AUTORES VIVOS EM DETERMINADO ANO
                5 - LISTAR LIVROS EM DETERMINADO IDIOMA
                6 - TOP 10 LIVROS
                7 - BUSCAR AUTORES POR NOME
                8 - MÉDIA DE DOWNLOADS POR AUTOR
                
                0 - S A I R
                ===================================================================================================================
                """;
        var opcao = -1;
        while (opcao != 0){
            System.out.println(menu);
            opcao = sc.nextInt();
            sc.nextLine();

            switch (opcao){
                case 1:
                    buscarNovoLivro();
                    break;
                case 2:
                    buscarLivrosRegistrados();
                    break;
                case 3:
                    buscarAutoresRegistrados();
                    break;
                case 4:
                    buscarAutoresVivosPorAno();
                    break;
                case 5:
                    buscarLivrosPorIdioma();
                    break;
                case 6:
                    buscarTop10();
                    break;
                case 7:
                    buscarAutorPorNome();
                    break;
                case 8:
                    mediaDeDownlaodsPorAutor();
                    break;
                case 0:
                    System.out.println("S A I N D O ...");
                    break;
                default:
                    System.out.println("\n\n=============   O p ç ã o   I n v á l i d a  (Digite um número de 0 a 8)   ================\n\n");
            }
        }


    }

    private void buscarNovoLivro() {
        System.out.println("\nQual livro deseja buscar?");
        var buscaDoUsuario = sc.nextLine();
        var dados = requisicao.consumo(ADDRESS+ buscaDoUsuario.replace(" ","%20"));
        salvarNoDb(dados);
    }

    private void salvarNoDb(String dados){
        try{
            Livro livro = new Livro(conversor.getData(dados, DadosLivro.class));
            Autor autor = new Autor(conversor.getData(dados, DadosAutor.class));
            Autor autorDb = null;
            Livro livroDb = null;
            if (!repositorioAutor.existsByNome(autor.getNome())){
                repositorioAutor.save(autor);
                autorDb = autor;
            }else{
                autorDb = repositorioAutor.findByNome(autor.getNome());
            }
            if (!repositorioLivro.existsByNome(livro.getNome())){
                livro.setAutor(autorDb);
                repositorioLivro.save(livro);
                livroDb = livro;
            }else{
                livroDb = repositorioLivro.findByNome(livro.getNome());
            }
            System.out.println(livroDb);
        }catch (NullPointerException e){
            System.out.println("\n\n=============   L i v r o   n ã o   e n c o n t r a d o     ================\n\n");
        }

    }


    private void buscarLivrosRegistrados() {
        var bucasDB = repositorioLivro.findAll();
        if(!bucasDB.isEmpty()){
            System.out.println("\nLivros cadastrados no Banco de Dados: ");
            bucasDB.forEach(System.out::println);
        }else{
            System.out.println("\nNenhum livro encontrado no Banco de Dados!");
        }
    }

    private void buscarAutoresRegistrados() {
        var buscaDb = repositorioAutor.findAll();
        if(!buscaDb.isEmpty()){
            System.out.println("\nAutores cadastrados no Banco de Dados:");
            buscaDb.forEach(System.out::println);
        }else{
            System.out.println("\nNenhum autor encontrado no Banco de Dados!");
        }
    }

    private void buscarAutoresVivosPorAno() {
        System.out.println("\nQual ano deseja pesquisar?");
        var anoSelecionado = sc.nextInt();
        sc.nextLine();
        var buscaAutoresNoDb = repositorioAutor.buscarPorAnoDeFalecimento(anoSelecionado);
        if(!buscaAutoresNoDb.isEmpty()){
            System.out.println("\n\nAtores vivos no ano de: " + anoSelecionado);
            buscaAutoresNoDb.forEach(System.out::println);
        }else {
            System.out.println("\nNenhum autor encontrado para esta data!");
        }
    }

    private void buscarLivrosPorIdioma() {
        var idiomasCadastrados = repositorioLivro.bucasidiomas();
        System.out.println("\nIdiomas cadastrados no Banco de Dados:");
        idiomasCadastrados.forEach(System.out::println);
        System.out.println("\nSelecione um dos idiomas cadastrados no Banco de Dados:\n");
        var idiomaSelecionado = sc.nextLine();
        repositorioLivro.buscarPorIdioma(idiomaSelecionado).forEach(System.out::println);
    }

    private void buscarTop10() {
        var top10 = repositorioLivro.findTop10ByOrderByQuantidadeDeDownloadsDesc();
        System.out.println("\n\n=============   T O P   1 0   M E L H O R E S   L I V R O S      ================\n\n");
        top10.forEach(System.out::println);
    }

    private void buscarAutorPorNome() {
        System.out.println("Qual o nome do autor?");
        var pesquisa = sc.nextLine();
        var autor = repositorioAutor.encontrarPorNome(pesquisa);
        if (!autor.isEmpty()){
            autor.forEach(System.out::println);
        }else{
            System.out.println("*** Autor não encontrado! ***");
        }
    }

    private void mediaDeDownlaodsPorAutor() {
        System.out.println("Qual autor deseja buscar?");
        var pesquisa = sc.nextLine();
        var test = repositorioLivro.encontrarLivrosPorAutor(pesquisa);
        DoubleSummaryStatistics media = test.stream()
                .mapToDouble(Livro::getQuantidadeDeDownloads)
                .summaryStatistics();
        System.out.println("Média de Downloads: "+ media.getAverage());
    }


}
