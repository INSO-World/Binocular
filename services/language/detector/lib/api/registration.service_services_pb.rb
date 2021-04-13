# Generated by the protocol buffer compiler.  DO NOT EDIT!
# Source: registration.service.proto for package 'binocular.comm'

require 'grpc'
require 'registration.service_pb'

module Binocular
  module Comm
    module RegistrationService
      class Service

        include GRPC::GenericService

        self.marshal_class_method = :encode
        self.unmarshal_class_method = :decode
        self.service_name = 'binocular.comm.RegistrationService'

        # register new service to the gateway
        rpc :register, RegistrationRequest, RegistrationResponse
        # unregister service from gateway
        rpc :unregister, UnregisterRequest, UnregisterResponse
        # heartbeat
        rpc :pulse, HeartbeatRequest, EmptyResponse
      end

      Stub = Service.rpc_stub_class
    end
  end
end
