rsync -aztv html root@47.108.150.14:/data/center_server-j21
if [ $? != 0 ]; then
  echo "上传 html 失败"
  exit 1
fi
