package fr.insee.queen.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.queen.api.domain.Operation;
import fr.insee.queen.api.dto.operation.OperationDto;
import fr.insee.queen.api.repository.OperationRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
* OperationController is the Controller using to manage {@link Operation} entity
* 
* @author Claudel Benjamin
* 
*/
@RestController
@RequestMapping
public class OperationController {
	/**
	* The operation repository using to access to table 'operation' in DB 
	*/
	@Autowired
	private OperationRepository operationRepository;
	
	/**
	* This method is using to get all operations
	* 
	* @return List of all {@link OperationDto}
	*/
	@ApiOperation(value = "Get list of operations")
	@GetMapping(path = "/operations")
	public List<OperationDto> getListOperation(){
		return operationRepository.findDtoBy();
	}
	
}
