package com.github.thundax.bacon.upms.api.facade;

import com.github.thundax.bacon.upms.api.request.DepartmentCodeGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.DepartmentGetFacadeRequest;
import com.github.thundax.bacon.upms.api.request.DepartmentListFacadeRequest;
import com.github.thundax.bacon.upms.api.response.DepartmentFacadeResponse;
import com.github.thundax.bacon.upms.api.response.DepartmentListFacadeResponse;

public interface DepartmentReadFacade {

    DepartmentFacadeResponse getDepartmentById(DepartmentGetFacadeRequest request);

    DepartmentFacadeResponse getDepartmentByCode(DepartmentCodeGetFacadeRequest request);

    DepartmentListFacadeResponse listByIds(DepartmentListFacadeRequest request);
}
