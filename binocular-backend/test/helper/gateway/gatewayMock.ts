'use strict';

import ServiceProviderMock from './serviceProviderMock.ts';
import GatewayService from '../../../utils/gateway-service';

class GatewayMock extends GatewayService {
  private serviceProvider: ServiceProviderMock;
  constructor() {
    super();
    this.serviceProvider = new ServiceProviderMock();
  }

  getServiceByType(): any {
    return this.serviceProvider;
  }
}

export default GatewayMock;
