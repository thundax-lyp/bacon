/**
 * 提供项目统一的 Web 表达层能力，包括标准响应结构、统一异常处理和返回包装。
 *
 * <p>边界约定：</p>
 * <ul>
 *     <li>面向前端或外部调用方的业务 Controller 可以通过
 *     {@link com.github.thundax.bacon.common.web.annotation.WrappedApiController}
 *     接入统一 ApiResponse 包装。</li>
 *     <li>{@code interfaces.provider} 下的 ProviderController 不应接入该包装，
 *     以保持内部调用契约稳定、直接且无额外响应壳。</li>
 *     <li>OAuth2、支付回调等协议型接口如需保持原始响应格式，也不应接入该包装。</li>
 * </ul>
 */
package com.github.thundax.bacon.common.web;
