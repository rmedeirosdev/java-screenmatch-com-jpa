package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.Repository.SerieRepository;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=".trim();
    //private final String API_KEY = "&apikey=175bf8e6";
    private final String API_KEY = System.getenv("API_KEY");
    private List <DadosSerie> seriesBuscadas = new ArrayList<>();
    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        int opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3- Listar séries
                    0 - Sair                                 
                    """;
               try {
                   System.out.println(menu);
                   opcao = Integer.parseInt(leitura.nextLine());
               } catch (NumberFormatException e) {
                   System.out.println("Po patrão, digita um número né!!!");
                   opcao = -1;
               }
                switch (opcao) {
                    case 1:
                        buscarSerieWeb();
                        break;
                    case 2:
                        buscarEpisodioPorSerie();
                        break;
                    case 3:
                        listarSeriesBuscadas();
                        break;
                    case 0:
                        System.out.println("Saindo...");
                        break;
                    default:
                        System.out.println("\nOpção inválida\n");
                }
        }
    }

    private void listarSeriesBuscadas() {
        List<Serie> series = repositorio.findAll();
       // List<Serie> series= seriesBuscadas.stream().map(d->new Serie(d)).collect(Collectors.toList());
        series.stream().sorted(Comparator.comparing(Serie::getGenero)).forEach(System.out::println);
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        seriesBuscadas.add(dados);
        Serie serie = new Serie(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        String nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        series= repositorio.findAll();
        System.out.println("Selecione uma série");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = series.stream().filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase())).findFirst();


        if (serie.isPresent()) {
            var seriebuscada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();
            for (int i = 1; i <= seriebuscada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + seriebuscada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

           List<Episodio> episodios =  temporadas.stream().flatMap(d -> d.episodios().stream().map(e -> new Episodio(d.numero(), e))).collect(Collectors.toList());
            seriebuscada.setEpisodios(episodios);
            repositorio.save(seriebuscada);
        } else {
            System.out.println("Série não encontrada!");
        }
    }
}