FROM mongo:8.0

ENV GLIBC_TUNABLES=glibc.pthread.rseq=1

EXPOSE 27017

CMD ["mongod", "--bind_ip_all", "--wiredTigerCacheSizeGB", "0.25"]
