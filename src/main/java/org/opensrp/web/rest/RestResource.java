package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.ParseException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

public abstract class RestResource <T>{

	@Autowired
	public ObjectMapper objectMapper;

	@RequestMapping(method=RequestMethod.POST, consumes={MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	private T createNew(@RequestBody T entity) {
		RestUtils.verifyRequiredProperties(requiredProperties(), entity);
		return create(entity);
	}
	
	@RequestMapping(value="/{uniqueId}", method=RequestMethod.POST, consumes={MediaType.APPLICATION_JSON_VALUE})
	@ResponseBody
	private T updateExisting(@PathVariable("uniqueId") String uniqueId, @RequestBody T entity) {
		RestUtils.verifyRequiredProperties(requiredProperties(), entity);
		return update(entity);//TODO
	}
	
	@RequestMapping(value="/{uniqueId}", method=RequestMethod.GET)
	@ResponseBody
	private T getById(@PathVariable("uniqueId") String uniqueId){
		return getByUniqueId(uniqueId);
	}
	
	
	@RequestMapping(method=RequestMethod.GET, value="/search")
	@ResponseBody
	private List<T> searchBy(HttpServletRequest request) throws ParseException{
		return search(request);
	}
	
	@RequestMapping(method=RequestMethod.GET)
	@ResponseBody
	private List<T> filterBy(@RequestParam(value="q", required=true) String query){
		return filter(query);
	}
	
	public abstract List<T> filter(String query) ;

	public abstract List<T> search(HttpServletRequest request) throws ParseException;
	
	public abstract T getByUniqueId(String uniqueId);
	
	public abstract List<String> requiredProperties();

	public abstract T create(T entity) ;

	public abstract T update(T entity) ;

}
