package servicios;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import interfaces.InterfazContactoSim;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.ResultadosApi;
import io.swagger.client.model.ResultsResponse;
import io.swagger.client.model.SolicitudResponse;
import modelo.DatosSimulation;
import modelo.DatosSolicitud;
import modelo.Entidad;

@Service
public class SimuladorService implements InterfazContactoSim {

	private DatosSolicitud sol;

	@Override
	public int solicitarSimulation(DatosSolicitud sol) {
		try {
	        String url = "http://localhost:8080/Solicitud/Solicitar?nombreUsuario=anmurog";

	        java.util.List<Integer> cantidades = new java.util.ArrayList<>(sol.getNums().values());
	        java.util.List<String> nombres = Arrays.asList("A", "B", "C");

	        java.util.Map<String, Object> body = new java.util.HashMap<>();
	        body.put("cantidadesIniciales", cantidades);
	        body.put("nombreEntidades", nombres);

	        RestTemplate restTemplate = new RestTemplate();
	        SolicitudResponse response = restTemplate.postForObject(url, body, SolicitudResponse.class);

	        if (response != null && response.isDone()) {
	            return response.getTokenSolicitud();
	        }
	        return -1;
	    } catch (Exception e) {
	        System.err.println("Error pidiendo token: " + e.getMessage());
	        return -1;
	    }
	}

	@Override
	public DatosSimulation descargarDatos(int ticket) {
		try {
	        String url = "http://localhost:8080/Resultados?nombreUsuario=anmurog&tok=" + ticket;
	        RestTemplate restTemplate = new RestTemplate();
	        
	        ResultsResponse response = restTemplate.postForObject(url, null, ResultsResponse.class);

	        if (response == null || !response.isDone() || response.getData() == null) {
	            return null;
	        }


	        String dataRaw = response.getData();
	        String[] lineas = dataRaw.split("\n"); 
	        
	        DatosSimulation ds = new DatosSimulation();

	        ds.setAnchoTablero(Integer.parseInt(lineas[0].trim()));
	        
	        java.util.Map<Integer, List<modelo.Punto>> mapaPuntos = new java.util.HashMap<>();
	        int maxSeg = 0;


	        for (int i = 1; i < lineas.length; i++) {
	            String[] partes = lineas[i].split(",");
	            if (partes.length == 4) {
	                int t = Integer.parseInt(partes[0].trim());
	                int y = Integer.parseInt(partes[1].trim());
	                int x = Integer.parseInt(partes[2].trim());
	                String color = partes[3].trim();

	                modelo.Punto p = new modelo.Punto();
	                p.setX(x);
	                p.setY(y);
	                p.setColor(color);

	                mapaPuntos.computeIfAbsent(t, k -> new java.util.ArrayList<>()).add(p);
	       
	                if (t > maxSeg) maxSeg = t;
	            }
	        }

	        ds.setPuntos(mapaPuntos);
	        ds.setMaxSegundos(maxSeg + 1);

	        return ds; 

	    } catch (Exception e) {
	        System.err.println("Error descargando datos: " + e.getMessage());
	        return null; 
	    }
	}

	@Override
	public List<Entidad> getEntities() {
		// TODO Auto-generated method stub
		Entidad e1 = new Entidad();
		e1.setName("A");
		e1.setId(0);

		Entidad e2 = new Entidad();
		e2.setName("B");
		e2.setId(1);

		Entidad e3 = new Entidad();
		e3.setName("C");
		e3.setId(2);

		return Arrays.asList(e1, e2, e3);
	}

	public List<String> obtenerGrid(String token) {
	    try {
	        ApiClient client = new ApiClient();
	        client.setBasePath("http://localhost:8080");
	        ResultadosApi api = new ResultadosApi(client);

	        ResultsResponse respuesta = api.resultadosPost("user", Integer.parseInt(token));
	        
	        String datosBrutos = respuesta.getData();
	        return java.util.Arrays.asList(datosBrutos.split(","));

	    } catch (ApiException e) {
	        e.printStackTrace();
	        return java.util.Collections.emptyList(); // Devuelve lista vacía si falla
	    }
	}

	@Override
	public boolean isValidEntityId(int id) {
		if (id == 0 || id == 1 || id == 2) {
	        return true;
	    }
	    return false;
	}

}
