package apirestazureread.function;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.management.relation.RoleNotFoundException;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import apirestazureread.model.Role;
import apirestazureread.service.RoleService;

public class RoleFunction {

    private final RoleService roleService = new RoleService();

    // Obtener todos los roles con paginación
    @FunctionName("getAllRoles")
    public HttpResponseMessage getAllRoles(
        @HttpTrigger(name = "req", methods = {HttpMethod.GET}, route = "roles") HttpRequestMessage<Optional<String>> request,
        ExecutionContext context) {

        try {
            // Obtener parámetros de paginación
            String limitParam = request.getQueryParameters().get("limit");
            String offsetParam = request.getQueryParameters().get("offset");

            // Valores por defecto
            int limit = (limitParam != null) ? Integer.parseInt(limitParam) : 10;
            int offset = (offsetParam != null) ? Integer.parseInt(offsetParam) : 0;

            // Llamar al servicio
            List<Role> roles = roleService.getAllRoles(limit, offset);

            return request.createResponseBuilder(HttpStatus.OK)
                          .body(roles)
                          .header("Content-Type", "application/json")
                          .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                          .body("{\"error\": \"Error fetching roles: " + e.getMessage() + "\"}")
                          .build();
        }
    }


    @FunctionName("getRoleById")
    public HttpResponseMessage getRoleById(
        @HttpTrigger(name = "req", methods = {HttpMethod.GET}, route = "roles/{id}") HttpRequestMessage<Optional<String>> request,
        @BindingName("id") String id, ExecutionContext context) {

        try {
            Role role = roleService.getRoleById(Long.valueOf(id)); // Este método puede lanzar RoleNotFoundException
            return request.createResponseBuilder(HttpStatus.OK)
                            .body(role)
                            .header("Content-Type", "application/json")
                            .build();
        } catch (RoleNotFoundException e) {  // Capturamos la excepción personalizada
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .body("{\"error\": \"" + e.getMessage() + "\"}")
                            .build();
        } catch (SQLException e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("{\"error\": \"Database error: " + e.getMessage() + "\"}")
                            .build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                            .body("{\"error\": \"Error fetching role: " + e.getMessage() + "\"}")
                            .build();
        }
    }
}
