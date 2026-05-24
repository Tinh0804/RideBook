const CustomerPromotionsPage = () => {
    return (
        <div className="min-h-screen bg-gray-50">
            {/* Hero Section */}
            <div className="relative h-64 bg-gradient-to-r from-primary-600 to-primary-700 overflow-hidden flex items-center justify-center">
                <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-10"></div>
                <div className="absolute top-0 right-0 w-96 h-96 bg-white/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
                <h1 className="text-5xl font-extrabold text-white tracking-tight drop-shadow-lg">
                    Ưu Đãi Độc Quyền
                </h1>
            </div>

            {/* Promotions Grid */}
            <div className="container mx-auto px-4 py-12">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                    {/* Promotion Card 1 */}
                    <div className="bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
                        <div className="relative">
                            <img
                                src="https://images.unsplash.com/photo-1542751371-adc2131af163?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
                                alt="Early Bird"
                                className="w-full h-48 object-cover"
                            />
                            <div className="absolute top-3 right-3 bg-secondary-500 text-white text-xs font-bold px-2 py-1 rounded-full">
                                NEW
                            </div>
                        </div>
                        <div className="p-6">
                            <h3 className="text-xl font-bold text-gray-800 mb-2">Ưu Đãi Đặt Sớm</h3>
                            <p className="text-gray-600 mb-4 line-clamp-3">
                                Đặt xe trước 7 ngày để nhận ngay ưu đãi 15% cho toàn bộ
                                hành trình.
                            </p>
                            <div className="flex items-center justify-between mb-4">
                                <span className="text-2xl font-bold text-primary-600">
                                    15% OFF
                                </span>
                                <span className="text-sm text-gray-500">
                                    Áp dụng đến 31/12/2024
                                </span>
                            </div>
                            <button className="w-full bg-primary-600 text-white py-2 rounded-lg hover:bg-primary-700 transition-colors duration-300">
                                Xem chi tiết
                            </button>
                        </div>
                    </div>

                    {/* Promotion Card 2 */}
                    <div className="bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
                        <div className="relative">
                            <img
                                src="https://images.unsplash.com/photo-1504877492558-417e74b78840?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
                                alt="Weekend Deal"
                                className="w-full h-48 object-cover"
                            />
                        </div>
                        <div className="p-6">
                            <h3 className="text-xl font-bold text-gray-800 mb-2">
                                Ưu Đãi Cuối Tuần
                            </h3>
                            <p className="text-gray-600 mb-4 line-clamp-3">
                                Giảm giá 20% khi đặt xe từ thứ 6 đến Chủ Nhật hàng tuần.
                            </p>
                            <div className="flex items-center justify-between mb-4">
                                <span className="text-2xl font-bold text-secondary-600">
                                    20% OFF
                                </span>
                                <span className="text-sm text-gray-500">
                                    Chỉ áp dụng cuối tuần
                                </span>
                            </div>
                            <button className="w-full bg-secondary-600 text-white py-2 rounded-lg hover:bg-secondary-700 transition-colors duration-300">
                                Xem chi tiết
                            </button>
                        </div>
                    </div>

                    {/* Promotion Card 3 */}
                    <div className="bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
                        <div className="relative">
                            <img
                                src="https://images.unsplash.com/photo-1581090464777-f321c957e1bf?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
                                alt="Group Discount"
                                className="w-full h-48 object-cover"
                            />
                        </div>
                        <div className="p-6">
                            <h3 className="text-xl font-bold text-gray-800 mb-2">
                                Ưu Đãi Nhóm
                            </h3>
                            <p className="text-gray-600 mb-4 line-clamp-3">
                                Giảm đến 30% khi đặt xe cho nhóm từ 5 người trở lên.
                            </p>
                            <div className="flex items-center justify-between mb-4">
                                <span className="text-2xl font-bold text-primary-600">
                                    Up to 30% OFF
                                </span>
                                <span className="text-sm text-gray-500">
                                    Áp dụng cho nhóm 5+ người
                                </span>
                            </div>
                            <button className="w-full bg-primary-600 text-white py-2 rounded-lg hover:bg-primary-700 transition-colors duration-300">
                                Xem chi tiết
                            </button>
                        </div>
                    </div>

                    {/* Promotion Card 4 */}
                    <div className="bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
                        <div className="relative">
                            <img
                                src="https://images.unsplash.com/photo-1525609004556-c46c7d6cf023?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
                                alt="Student Discount"
                                className="w-full h-48 object-cover"
                            />
                        </div>
                        <div className="p-6">
                            <h3 className="text-xl font-bold text-gray-800 mb-2">
                                Ưu Đãi Sinh Viên
                            </h3>
                            <p className="text-gray-600 mb-4 line-clamp-3">
                                Giảm 10% cho tất cả các chuyến đi khi xuất trình thẻ sinh viên.
                            </p>
                            <div className="flex items-center justify-between mb-4">
                                <span className="text-2xl font-bold text-secondary-600">
                                    10% OFF
                                </span>
                                <span className="text-sm text-gray-500">
                                    Yêu cầu thẻ sinh viên
                                </span>
                            </div>
                            <button className="w-full bg-secondary-600 text-white py-2 rounded-lg hover:bg-secondary-700 transition-colors duration-300">
                                Xem chi tiết
                            </button>
                        </div>
                    </div>

                    {/* Promotion Card 5 */}
                    <div className="bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-xl transition-all duration-300 hover:-translate-y-1">
                        <div className="relative">
                            <img
                                src="https://images.unsplash.com/photo-1519389950473-471779a99f5d?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"
                                alt="Silver Member"
                                className="w-full h-48 object-cover"
                            />
                        </div>
                        <div className="p-6">
                            <h3 className="text-xl font-bold text-gray-800 mb-2">
                                Ưu Đãi Thành Viên Bạc
                            </h3>
                            <p className="text-gray-600 mb-4 line-clamp-3">
                                Giảm 5% cho tất cả các chuyến đi hàng tuần.
                            </p>
                            <div className="flex items-center justify-between mb-4">
                                <span className="text-2xl font-bold text-secondary-600">
                                    5% OFF
                                </span>
                                <span className="text-sm text-gray-500">
                                    Áp dụng cho thành viên Bạc
                                </span>
                            </div>
                            <button className="w-full bg-secondary-600 text-white py-2 rounded-lg hover:bg-secondary-700 transition-colors duration-300">
                                Xem chi tiết
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CustomerPromotionsPage;