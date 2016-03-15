# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  
  config.vm.provider :virtualbox do |vb|
    vb.customize ["modifyvm", :id, "--memory", "1024"]
    vb.name = "bb-learn-9.1.201410.160373"
  end

  config.vm.box = 'bb-learn-9.1.201410.160373'  
  config.vm.box_url = './bb-learn-9.1.201410.160373.box'
  config.vm.network :forwarded_port, guest: 8443, host: 9877
  config.vm.network :forwarded_port, guest: 2222, host: 9878
#  config.vm.network "public_network"

  config.vm.provision "shell",
    # remove insecure ciphers to fix ERR_SSL_WEAK_SERVER_EPHEMERAL_DH_KEY issue
    inline: 'sudo sed -i "s/, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, SSL_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA//" /usr/local/blackboard/config/bb-config.properties && sudo sed -i "s/, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, SSL_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA//" /usr/local/blackboard/apps/tomcat/conf/server.xml'

  config.vm.post_up_message = "Default BB admin account: administrator/password. \nDB user: postgres/postgres. \nTo start/stop BB service: sudo /usr/local/blackboard/tools/admin/ServiceController.sh services.stop/start"
end
