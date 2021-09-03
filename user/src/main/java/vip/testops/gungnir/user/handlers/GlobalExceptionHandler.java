package vip.testops.gungnir.user.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vip.testops.gungnir.user.commons.Response;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public Response<?> resolveConstraintViolationException(ConstraintViolationException ex){
        Response<?> response = new Response<>();

        Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
        if(!CollectionUtils.isEmpty(constraintViolations)) {
            StringBuilder stringBuilder = new StringBuilder();
            for(ConstraintViolation<?> constraintViolation : constraintViolations) {
                stringBuilder.append(constraintViolation.getMessage()).append(", ");
            }
            String errorMessage = stringBuilder.toString();
            if(errorMessage.length() >1 ){
                errorMessage = errorMessage.substring(0,errorMessage.length()-1);
            }
            response.setCode(2002);
            response.setMessage(errorMessage);
            return response;
        }
        response.setCode(2003);
        response.setMessage(ex.getMessage());
        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<?> resolveMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Response<?> response = new Response<>();
        List<ObjectError> objectErrors = ex.getBindingResult().getAllErrors();
        if(!CollectionUtils.isEmpty(objectErrors)){

            response.paramMissError(objectErrors.get(0).getDefaultMessage());
            return response;
        }
        response.setCode(2003);
        response.setMessage(ex.getMessage());
        return response;
    }
}
