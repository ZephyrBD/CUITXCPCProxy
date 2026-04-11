export const getStatusTagType = (status) => {
  switch(status) {
    case 'PENDING': return 'info';
    case 'ACCEPTED': case 'AUTO_DONE': case 'DONE': return 'success';
    case 'DENY': return 'danger';
    default: return 'info';
  }
};

export const getStatusText = (status) => {
  switch(status) {
    case 'PENDING': return '待处理';
    case 'ACCEPTED': return '已通过';
    case 'DENY': return '已拒绝';
    case 'DONE': return '已完成';
    case 'AUTO_DONE': return '自动处理';
    default: return status;
  }
};