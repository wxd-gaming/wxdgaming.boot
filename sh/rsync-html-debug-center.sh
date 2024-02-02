rsync -aztv html root@47.108.81.97:/data/game/d/center-server-j21-2
if [ $? != 0 ]; then
  echo "上传 html 失败"
  exit 1
fi