cd target/run
rsync -aztv server-center.jar root@47.108.81.97:/data/game/d/center-server-j21-2
if [ $? != 0 ]; then
  echo "上传 server-center.jar 失败"
  exit 1
fi
rsync -aztv script.jar root@47.108.81.97:/data/game/d/center-server-j21-2
if [ $? != 0 ]; then
  echo "上传 script.jar 失败"
  exit 1
fi
rsync -aztv html root@47.108.81.97:/data/game/d/center-server-j21-2
if [ $? != 0 ]; then
  echo "上传 html 失败"
  exit 1
fi
rsync -aztv service.sh root@47.108.150.14:/data/center_server_j21
if [ $? != 0 ]; then
  echo "上传 service.sh 失败"
  exit 1
fi