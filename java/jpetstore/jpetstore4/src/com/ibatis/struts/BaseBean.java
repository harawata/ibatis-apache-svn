package com.ibatis.struts;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionError;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * All actions mapped through the BeanAction class should be mapped
 * to a subclass of BaseBean (or have no form bean mapping at all).
 * <p/>
 * The BaseBean class simplifies the validate() and reset() methods
 * by allowing them to be managed without Struts dependencies. Quite
 * simply, subclasses can override the parameterless validate()
 * and reset() methods and set errors and messages using the ActionContext
 * class.
 * <p/>
 * <i>Note:  Full error, message and internationalization support is not complete.</i>
 * <p/>
 * Date: Mar 12, 2004 9:20:39 PM
 * 
 * @author Clinton Begin
 */
public abstract class BaseBean extends ActionForm {

  public void reset(ActionMapping mapping, ServletRequest request) {
    ActionContext.initialize((HttpServletRequest)request, null);
    reset();
  }

  public void reset(ActionMapping mapping, HttpServletRequest request) {
    ActionContext.initialize((HttpServletRequest)request, null);
    reset();
  }

  public ActionErrors validate(ActionMapping mapping, ServletRequest request) {
    ActionContext.initialize((HttpServletRequest)request, null);
    ActionContext ctx = ActionContext.getActionContext();
    Map requestMap = ctx.getRequestMap();

    List errorList = null;
    requestMap.put("errors",errorList);
    validate();
    errorList = (List) requestMap.get("errors");
    ActionErrors actionErrors = null;
    if (errorList != null && !errorList.isEmpty()) {
      actionErrors = new ActionErrors();
      actionErrors.add(ActionErrors.GLOBAL_ERROR, new ActionError("global.error"));
    }
    return actionErrors;
  }

  public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
    ActionContext.initialize(request, null);
    ActionContext ctx = ActionContext.getActionContext();
    Map requestMap = ctx.getRequestMap();

    List errorList = null;
    requestMap.put("errors",errorList);
    validate();
    errorList = (List) requestMap.get("errors");
    ActionErrors actionErrors = null;
    if (errorList != null && !errorList.isEmpty()) {
      actionErrors = new ActionErrors();
      actionErrors.add(ActionErrors.GLOBAL_ERROR, new ActionError("global.error"));
    }
    return actionErrors;
  }

  public void validate() {
  }

  public void reset() {
  }

  public void clear() {
  }

  protected void validateRequiredField(String value, String errorMessage) {
    if (value == null || value.trim().length() < 1) {
      ActionContext.getActionContext().addSimpleError(errorMessage);
    }
  }

}
